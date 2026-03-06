/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package org.osgi.test.junit5.featurelauncher;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;
import org.osgi.test.common.service.ServiceAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureLaunchExtension
		implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

	private static final Logger LOG = LoggerFactory.getLogger(FeatureLaunchExtension.class);

	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
			.create(FeatureLaunchExtension.class);

	private static final String KEY_CAPTURE = "snapshotCapture";
	private static final String KEY_PROCESS = "process";
	private static final String KEY_CONNECTOR = "jdiConnector";
	private static final String KEY_BRIDGE = "bridge";
	private static final String KEY_SCOPED_CAPTURE = "scopedCapture";

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		Class<?> testClass = context.getRequiredTestClass();
		AnnotationSupport.findAnnotation(testClass, FeatureLaunch.class)
				.ifPresent(annotation -> startFramework(context, annotation, testClass));
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		// Check if class-level framework is already running
		JdiFrameworkBridge bridge = findClassLevelBridge(context);
		if (bridge != null) {
			// Create a method-scoped capture with its own service tracking
			store(context).put(KEY_SCOPED_CAPTURE, new FeatureLaunchCapture(bridge));
			return;
		}

		// No class-level framework — check for method-level @FeatureLaunch
		context.getTestMethod().flatMap(method -> AnnotationSupport.findAnnotation(method, FeatureLaunch.class))
				.ifPresent(annotation -> startFramework(context, annotation, context.getRequiredTestClass()));
	}

	@Override
	public void afterEach(ExtensionContext context) {
		// Close scoped capture (per-method service cleanup)
		FeatureLaunchCapture scoped = store(context).remove(KEY_SCOPED_CAPTURE, FeatureLaunchCapture.class);
		if (scoped != null) {
			scoped.close();
		}

		// If method-level framework was started, stop it
		if (store(context).get(KEY_CAPTURE) != null) {
			stopFramework(context);
		}
	}

	@Override
	public void afterAll(ExtensionContext context) {
		if (store(context).get(KEY_CAPTURE) != null) {
			stopFramework(context);
		}
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Class<?> type = parameterContext.getParameter().getType();
		return type == FeatureLaunchCapture.class || type == ServiceAware.class;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Class<?> type = parameterContext.getParameter().getType();

		if (type == ServiceAware.class) {
			return resolveServiceAware(parameterContext, extensionContext);
		}

		return resolveCapture(extensionContext);
	}

	private FeatureLaunchCapture resolveCapture(ExtensionContext extensionContext) {
		// Prefer scoped capture (method-level, per-method service tracking)
		FeatureLaunchCapture scoped = store(extensionContext).get(KEY_SCOPED_CAPTURE, FeatureLaunchCapture.class);
		if (scoped != null) {
			return scoped;
		}

		// Method-level framework capture
		FeatureLaunchCapture capture = store(extensionContext).get(KEY_CAPTURE, FeatureLaunchCapture.class);
		if (capture != null) {
			return capture;
		}

		// Walk up to parent context (method -> class)
		capture = findClassLevelCapture(extensionContext);
		if (capture != null) {
			return capture;
		}

		throw new ParameterResolutionException(
				"No FeatureLaunchCapture available. Ensure @FeatureLaunch is present on the test class or method.");
	}

	@SuppressWarnings("unchecked")
	private <T> ServiceAware<T> resolveServiceAware(ParameterContext parameterContext,
			ExtensionContext extensionContext) {
		Type genericType = parameterContext.getParameter().getParameterizedType();
		if (!(genericType instanceof ParameterizedType pt)) {
			throw new ParameterResolutionException(
					"ServiceAware parameter must be parameterized, e.g. ServiceAware<MyService>");
		}
		Type[] typeArgs = pt.getActualTypeArguments();
		if (typeArgs.length != 1 || !(typeArgs[0] instanceof Class)) {
			throw new ParameterResolutionException(
					"ServiceAware type argument must be a concrete class, e.g. ServiceAware<MyService>");
		}
		Class<T> serviceType = (Class<T>) typeArgs[0];

		FeatureLaunchCapture capture = resolveCapture(extensionContext);
		return capture.getServiceAware(serviceType);
	}

	// --- Framework lifecycle helpers ---

	private void startFramework(ExtensionContext context, FeatureLaunch annotation, Class<?> testClass) {
		try {
			doStartFramework(context, annotation, testClass);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Failed to start Feature Launcher", e);
		}
	}

	private void doStartFramework(ExtensionContext context, FeatureLaunch annotation, Class<?> testClass)
			throws Exception {
		int port = annotation.debugPort();
		if (port == 0) {
			port = findFreePort();
		}

		String launcherJar = resolveLauncherJar(annotation.launcherJar(), testClass);
		String featureFile = resolveFeatureFile(annotation.featureFile(), testClass);
		String[] classpath = buildClasspath(annotation.framework(), annotation.classpath());
		String[] artifactRepos = resolveArtifactRepositories(annotation.artifactRepositories());
		boolean useDefaultRepos = annotation.useDefaultRepos();
		String[] decorators = resolveProperties(annotation.decorators());
		String[] extensionHandlers = resolveProperties(annotation.extensionHandlers());
		String[] variableOverrides = resolveProperties(annotation.variableOverrides());
		String[] configuration = resolveProperties(annotation.configuration());
		String[] frameworkProps = resolveProperties(annotation.frameworkProperties());
		String workingDir = resolveProperty(annotation.workingDirectory());
		String[] jvmArgs = resolveProperties(annotation.jvmArgs());

		LOG.info("Starting Feature Launcher (jar={}, feature={}, port={})", launcherJar, featureFile, port);

		JdiLauncherProcess process = new JdiLauncherProcess(launcherJar, featureFile, classpath, artifactRepos,
				useDefaultRepos, decorators, extensionHandlers, variableOverrides, configuration, frameworkProps, port,
				workingDir, jvmArgs);

		process.start();
		store(context).put(KEY_PROCESS, process);

		if (!process.getProcess().isAlive()) {
			throw new IllegalStateException("Launcher process exited with code " + process.getProcess().exitValue());
		}

		JdiConnector connector = new JdiConnector();
		try {
			connector.attach(port, annotation.connectTimeoutMs(), annotation.connectMaxRetries());
		} catch (Exception e) {
			process.stop();
			throw e;
		}
		store(context).put(KEY_CONNECTOR, connector);

		JdiFrameworkBridge bridge = new JdiFrameworkBridge(connector.getVm());
		try {
			bridge.waitForFrameworkActive(annotation.startupTimeoutSeconds() * 1000L, annotation.pollIntervalMs());
			LOG.info("Framework is ACTIVE");
		} catch (Exception e) {
			connector.dispose();
			process.stop();
			throw e;
		}
		store(context).put(KEY_BRIDGE, bridge);

		connector.resume();

		int settleMs = annotation.serviceSettleMs();
		if (settleMs > 0) {
			LOG.info("Waiting {}ms for services to settle...", settleMs);
			Thread.sleep(settleMs);
		}

		store(context).put(KEY_CAPTURE, new FeatureLaunchCapture(bridge));
		LOG.info("FeatureLaunchCapture ready");
	}

	private void stopFramework(ExtensionContext context) {
		FeatureLaunchCapture capture = store(context).remove(KEY_CAPTURE, FeatureLaunchCapture.class);
		if (capture != null) {
			capture.close();
		}

		store(context).remove(KEY_BRIDGE, JdiFrameworkBridge.class);

		JdiConnector connector = store(context).remove(KEY_CONNECTOR, JdiConnector.class);
		if (connector != null) {
			connector.dispose();
		}

		JdiLauncherProcess process = store(context).remove(KEY_PROCESS, JdiLauncherProcess.class);
		if (process != null) {
			process.stop();
		}
	}

	private FeatureLaunchCapture findClassLevelCapture(ExtensionContext context) {
		return context.getParent()
				.map(parent -> parent.getStore(NAMESPACE).get(KEY_CAPTURE, FeatureLaunchCapture.class)).orElse(null);
	}

	private JdiFrameworkBridge findClassLevelBridge(ExtensionContext context) {
		return context.getParent().map(parent -> parent.getStore(NAMESPACE).get(KEY_BRIDGE, JdiFrameworkBridge.class))
				.orElse(null);
	}

	private ExtensionContext.Store store(ExtensionContext context) {
		return context.getStore(NAMESPACE);
	}

	// --- Property resolution helpers ---

	private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

	static String resolveProperty(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		Matcher matcher = PROPERTY_PATTERN.matcher(value);
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			String propName = matcher.group(1);
			String propValue = System.getProperty(propName);
			if (propValue == null) {
				throw new IllegalArgumentException("System property '" + propName
						+ "' is not set (referenced in annotation value '" + value + "')");
			}
			matcher.appendReplacement(sb, Matcher.quoteReplacement(propValue));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	static String[] resolveProperties(String[] values) {
		String[] resolved = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			resolved[i] = resolveProperty(values[i]);
		}
		return resolved;
	}

	static String resolveLauncherJar(String value, Class<?> testClass) {
		if (value != null && !value.isEmpty()) {
			return resolveProperty(value);
		}
		URL testClassLocation = testClass.getProtectionDomain().getCodeSource().getLocation();
		if (testClassLocation != null) {
			try {
				Path testClasses = Path.of(testClassLocation.toURI());
				Path launcher = testClasses.getParent().resolve("launcher.jar");
				if (Files.exists(launcher)) {
					return launcher.toString();
				}
			} catch (Exception e) {
				LOG.debug("Failed to auto-discover launcher jar from test class location", e);
			}
		}
		throw new IllegalStateException(
				"Cannot auto-discover launcher.jar. Add maven-dependency-plugin to copy it to target/, "
						+ "or set launcherJar explicitly in @FeatureLaunch.");
	}

	static String resolveFeatureFile(String value, Class<?> testClass) {
		String resolved = resolveProperty(value);
		if (Path.of(resolved).isAbsolute()) {
			return resolved;
		}
		URL resource = testClass.getClassLoader().getResource(resolved);
		if (resource != null) {
			try {
				return Path.of(resource.toURI()).toString();
			} catch (Exception e) {
				throw new IllegalStateException("Failed to resolve feature file: " + resolved, e);
			}
		}
		throw new IllegalStateException("Feature file not found on classpath: " + resolved);
	}

	static String[] resolveArtifactRepositories(String[] values) {
		if (values == null || values.length == 0) {
			return new String[] { Path.of(System.getProperty("user.home"), ".m2", "repository").toString() };
		}
		return resolveProperties(values);
	}

	static String[] buildClasspath(String framework, String[] extraClasspath) {
		List<String> cp = new ArrayList<>();
		if (framework != null && !framework.isEmpty()) {
			cp.add(resolveFrameworkToJar(framework));
		}
		for (String entry : extraClasspath) {
			String resolved = resolveProperty(entry);
			if (!resolved.isEmpty()) {
				cp.add(resolved);
			}
		}
		return cp.toArray(String[]::new);
	}

	static String resolveFrameworkToJar(String mavenCoordinate) {
		String[] parts = mavenCoordinate.split(":");
		if (parts.length < 3) {
			throw new IllegalArgumentException(
					"Invalid Maven coordinate (expected groupId:artifactId:version): " + mavenCoordinate);
		}
		String groupId = parts[0];
		String artifactId = parts[1];
		String version = parts[2];

		Path localRepo = Path.of(System.getProperty("user.home"), ".m2", "repository");
		Path jar = localRepo.resolve(groupId.replace('.', '/')).resolve(artifactId).resolve(version)
				.resolve(artifactId + "-" + version + ".jar");

		if (!Files.exists(jar)) {
			throw new IllegalStateException("Framework JAR not found in local Maven repository: " + jar
					+ " (coordinate: " + mavenCoordinate + ")");
		}
		LOG.debug("Resolved framework {} to {}", mavenCoordinate, jar);
		return jar.toString();
	}

	private static int findFreePort() {
		try (ServerSocket socket = new ServerSocket(0)) {
			socket.setReuseAddress(true);
			return socket.getLocalPort();
		} catch (IOException e) {
			throw new IllegalStateException("Unable to find a free TCP port", e);
		}
	}
}

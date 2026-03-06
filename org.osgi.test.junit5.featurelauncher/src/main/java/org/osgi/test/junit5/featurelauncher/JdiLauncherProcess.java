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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JdiLauncherProcess {

	private static final Logger LOG = LoggerFactory.getLogger(JdiLauncherProcess.class);

	private final String launcherJar;
	private final String featureFile;
	private final String[] classpath;
	private final String[] artifactRepositories;
	private final boolean useDefaultRepos;
	private final String[] decorators;
	private final String[] extensionHandlers;
	private final String[] variableOverrides;
	private final String[] configuration;
	private final String[] frameworkProperties;
	private final int debugPort;
	private final String workingDirectory;
	private final String[] jvmArgs;

	private Process process;

	public JdiLauncherProcess(String launcherJar, String featureFile, String[] classpath, String[] artifactRepositories,
			boolean useDefaultRepos, String[] decorators, String[] extensionHandlers, String[] variableOverrides,
			String[] configuration, String[] frameworkProperties, int debugPort, String workingDirectory,
			String[] jvmArgs) {
		this.launcherJar = launcherJar;
		this.featureFile = featureFile;
		this.classpath = classpath;
		this.artifactRepositories = artifactRepositories;
		this.useDefaultRepos = useDefaultRepos;
		this.decorators = decorators;
		this.extensionHandlers = extensionHandlers;
		this.variableOverrides = variableOverrides;
		this.configuration = configuration;
		this.frameworkProperties = frameworkProperties;
		this.debugPort = debugPort;
		this.workingDirectory = workingDirectory;
		this.jvmArgs = jvmArgs;
	}

	public Process start() throws IOException {
		Path java = Paths.get(System.getProperty("java.home"), "bin", "java");

		List<String> command = new ArrayList<>();
		command.add(java.toAbsolutePath().toString());

		// JDWP debug agent
		command.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:" + debugPort);

		// Additional JVM arguments
		for (String arg : jvmArgs) {
			if (!arg.isEmpty()) {
				command.add(arg);
			}
		}

		// Classpath: launcher JAR + additional entries
		String cp = Stream.concat(Stream.of(launcherJar), Stream.of(classpath).filter(s -> !s.isEmpty()))
				.collect(Collectors.joining(File.pathSeparator));
		command.add("-cp");
		command.add(cp);

		// Main class from launcher JAR manifest
		command.add(readMainClass(launcherJar));

		// Artifact repositories
		for (String repo : artifactRepositories) {
			command.add("-a");
			command.add(repo);
		}

		// Default repos flag
		if (useDefaultRepos) {
			command.add("--impl-default-repos");
		}

		// Feature file
		command.add("-f");
		command.add(featureFile);

		addFlaggedArgs(command, decorators, "-d");
		addFlaggedArgs(command, extensionHandlers, "-e");
		addFlaggedArgs(command, variableOverrides, "-v");
		addFlaggedArgs(command, configuration, "-c");
		addFlaggedArgs(command, frameworkProperties, "-l");

		LOG.info("Starting Feature Launcher: {}", String.join(" ", command));

		ProcessBuilder builder = new ProcessBuilder(command);
		Path logFile = Path.of(System.getProperty("user.dir"), "target", "launcher-process.log");
		Files.createDirectories(logFile.getParent());
		builder.redirectErrorStream(true);
		builder.redirectOutput(logFile.toFile());

		if (workingDirectory != null && !workingDirectory.isEmpty()) {
			Path workDir = Paths.get(workingDirectory);
			if (Files.isDirectory(workDir)) {
				builder.directory(workDir.toFile());
			}
		}

		process = builder.start();
		LOG.info("Feature Launcher process started (PID: {}, debug port: {})", process.pid(), debugPort);
		return process;
	}

	public void stop() {
		if (process == null || !process.isAlive()) {
			return;
		}

		LOG.info("Stopping Feature Launcher process (PID: {})", process.pid());
		process.destroy();

		try {
			if (!process.waitFor(10, TimeUnit.SECONDS)) {
				LOG.warn("Process did not stop gracefully, destroying forcibly");
				process.destroyForcibly();
				process.waitFor(5, TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			process.destroyForcibly();
		}

		LOG.info("Feature Launcher process stopped (exit code: {})",
				process.isAlive() ? "still running" : process.exitValue());
	}

	public Process getProcess() {
		return process;
	}

	private static void addFlaggedArgs(List<String> command, String[] values, String flag) {
		for (String value : values) {
			if (!value.isEmpty()) {
				command.add(flag);
				command.add(value);
			}
		}
	}

	private static String readMainClass(String jarPath) throws IOException {
		try (JarFile jar = new JarFile(jarPath)) {
			String mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");
			if (mainClass == null || mainClass.isEmpty()) {
				throw new IllegalStateException("No Main-Class found in manifest of " + jarPath);
			}
			return mainClass;
		}
	}
}

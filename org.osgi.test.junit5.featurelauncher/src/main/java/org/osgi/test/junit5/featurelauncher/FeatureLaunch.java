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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(FeatureLaunchExtension.class)
public @interface FeatureLaunch {

	/**
	 * Path to the Feature Launcher JAR. If empty, auto-discovered from
	 * {@code target/launcher.jar} (copied by maven-dependency-plugin).
	 */
	String launcherJar() default "";

	/** Path to the feature.json file (classpath resource or absolute path) */
	String featureFile();

	/**
	 * OSGi framework artifact (groupId:artifactId:version). Resolved to a JAR in
	 * the local Maven repository and added to the JVM classpath. The launcher
	 * discovers the FrameworkFactory via ServiceLoader.
	 */
	String framework() default "";

	/**
	 * URI(s) for artifact repositories (passed as -a arguments). If empty, defaults
	 * to {@code ~/.m2/repository}.
	 */
	String[] artifactRepositories() default {};

	/** Use default local and remote repositories (--impl-default-repos flag) */
	boolean useDefaultRepos() default false;

	/** Decorator class names (passed as -d arguments) */
	String[] decorators() default {};

	/** Extension handler name=className pairs (passed as -e arguments) */
	String[] extensionHandlers() default {};

	/** Variable override key=value pairs (passed as -v arguments) */
	String[] variableOverrides() default {};

	/** Configuration key=value pairs (passed as -c arguments) */
	String[] configuration() default {};

	/** Framework properties key=value pairs (passed as -l arguments) */
	String[] frameworkProperties() default {};

	/** Additional classpath entries (JAR paths) for the launcher JVM */
	String[] classpath() default {};

	/** Debug port for JDWP (0 = auto-select free port) */
	int debugPort() default 0;

	/** Timeout in seconds for framework startup */
	int startupTimeoutSeconds() default 60;

	/** Poll interval in milliseconds for framework state check */
	int pollIntervalMs() default 500;

	/** Working directory for the launcher process (empty = system default) */
	String workingDirectory() default "";

	/** Additional JVM arguments (e.g., "-Xmx512m") */
	String[] jvmArgs() default {};

	/**
	 * Time in milliseconds to wait after framework ACTIVE before tests start.
	 * Allows asynchronous services (HTTP whiteboards, Jakarta RS, etc.) to settle.
	 */
	int serviceSettleMs() default 0;

	/** Timeout in milliseconds for each JDI attach attempt */
	int connectTimeoutMs() default 2000;

	/** Maximum number of JDI attach retries */
	int connectMaxRetries() default 5;
}

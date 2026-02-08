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
package org.osgi.test.example.demo.test;

import static org.osgi.test.common.annotation.Property.ValueSource.TestClass;
import static org.osgi.test.common.annotation.Property.ValueSource.TestMethod;
import static org.osgi.test.common.annotation.Property.ValueSource.TestUniqueId;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.TemplateArgument;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;

@ExtendWith(ConfigurationExtension.class)
public class Demo_13_Config_AnnotationsInAnnotation {

	@Test
	@WithFactoryConfiguration(factoryPid = FACTORY_CONFIGURATION_PID, properties = { @Property(key = "foo", value = "bar") })
	@WithFactoryConfiguration(factoryPid = "other", properties = { @Property(key = "fizz", value = "buzz") })
	@WithConfiguration(pid = "foo", properties = { @Property(key = "testName", source = TestClass), @Property(key = "testMethod", source = TestMethod),
			@Property(key = "testId", source = TestUniqueId),
			@Property(key = "testTemplate", value = { "Class : %s", "Test : %2$s", "Id : %3$s" }, type = Type.Collection, templateArguments = { @TemplateArgument(source = TestClass),
					@TemplateArgument(source = TestMethod), @TemplateArgument(source = TestUniqueId) }) })
	@WithConfiguration(pid = "bar", properties = { @Property(key = "testName", source = TestClass), @Property(key = "testMethod", source = TestMethod),
			@Property(key = "testId", source = TestUniqueId),
			@Property(key = "testTemplate", value = { "Class : %s", "Test : %2$s", "Id : %3$s" }, type = Type.Collection, templateArguments = { @TemplateArgument(source = TestClass),
					@TemplateArgument(source = TestMethod), @TemplateArgument(source = TestUniqueId) }) })
	@WithConfiguration(pid = "buzz", properties = { @Property(key = "testName", source = TestClass), @Property(key = "testMethod", source = TestMethod),
			@Property(key = "testId", source = TestUniqueId),
			@Property(key = "testTemplate", value = { "Class : %s", "Test : %2$s", "Id : %3$s" }, type = Type.Collection, templateArguments = { @TemplateArgument(source = TestClass),
					@TemplateArgument(source = TestMethod), @TemplateArgument(source = TestUniqueId) }) })
	@WithConfiguration(pid = "whoom", properties = { @Property(key = "testName", source = TestClass), @Property(key = "testMethod", source = TestMethod),
			@Property(key = "testId", source = TestUniqueId), @Property(key = "testTemplate", value = { "Class : %s", "Test : %2$s", "Id : %3$s" }, type = Type.Collection, templateArguments = {
					@TemplateArgument(source = TestClass), @TemplateArgument(source = TestMethod), @TemplateArgument(source = TestUniqueId) }) })
	public void tooManyConfigsToRead() throws Exception {

	}

	@Test
	@ConfigSetup_1
	void testSimple() throws Exception {

	}

	@Test
	@ConfigSetup_1
	@ConfigSetup_2
	void testOrderLatestWins() throws Exception {

	}

	private static final String KEY = "key";
	public static final String FACTORY_CONFIGURATION_PID = "my.factory.configuration.pid";
	public static final String CONFIGURATION_PID = "my.configuration.pid";
	public static final String NAME = "name";

	@WithConfiguration(pid = CONFIGURATION_PID, properties = { @Property(key = KEY, value = ConfigSetup_1.VALUE) })
	@WithFactoryConfiguration(factoryPid = FACTORY_CONFIGURATION_PID, name = NAME, properties = { @Property(key = KEY, value = ConfigSetup_1.VALUE) })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConfigSetup_1 {
		public static final String VALUE = "1";
	}

	@WithConfiguration(pid = CONFIGURATION_PID, properties = { @Property(key = KEY, value = ConfigSetup_2.VALUE_FIRST) })
	@WithFactoryConfiguration(factoryPid = FACTORY_CONFIGURATION_PID, name = NAME, properties = { @Property(key = KEY, value = ConfigSetup_2.VALUE_FIRST) })
	@WithConfiguration(pid = CONFIGURATION_PID, properties = { @Property(key = KEY, value = ConfigSetup_2.VALUE) })
	@WithFactoryConfiguration(factoryPid = FACTORY_CONFIGURATION_PID, name = NAME, properties = { @Property(key = KEY, value = ConfigSetup_2.VALUE) })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConfigSetup_2 {
		public static final String VALUE_FIRST = "2_FIRST";
		public static final String VALUE = "2";

	}

	@ConfigSetup_1
	@ConfigSetup_2
	@WithConfiguration(pid = CONFIGURATION_PID, properties = { @Property(key = KEY, value = ConfigSetup_A.VALUE) })
	@WithFactoryConfiguration(factoryPid = FACTORY_CONFIGURATION_PID, name = NAME, properties = { @Property(key = KEY, value = ConfigSetup_A.VALUE) })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConfigSetup_A {
		public static final String VALUE = "A";

	}

	@WithConfiguration(pid = CONFIGURATION_PID, properties = { @Property(key = KEY, value = ConfigSetup_B.VALUE) })
	@WithFactoryConfiguration(factoryPid = FACTORY_CONFIGURATION_PID, name = NAME, properties = { @Property(key = KEY, value = ConfigSetup_B.VALUE) })
	@ConfigSetup_1
	@ConfigSetup_2
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConfigSetup_B {
		public static final String VALUE = "B";

	}

}
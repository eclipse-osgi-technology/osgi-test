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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.osgi.test.common.annotation.Property.Scalar.Byte;
import static org.osgi.test.common.annotation.Property.Type.PrimitiveArray;
import static org.osgi.test.common.annotation.Property.ValueSource.EnvironmentVariable;
import static org.osgi.test.common.annotation.Property.ValueSource.SystemProperty;
import static org.osgi.test.common.annotation.Property.ValueSource.TestClass;
import static org.osgi.test.common.annotation.Property.ValueSource.TestMethod;
import static org.osgi.test.common.annotation.Property.ValueSource.TestUniqueId;

import java.util.Dictionary;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Scalar;
import org.osgi.test.common.annotation.Property.TemplateArgument;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.junit5.cm.ConfigUtil;

@WithConfiguration(pid = Demo_12_Configuration.NONSTATIC_CONFIGURATION_PID)
public class Demo_12_Configuration {

	public static final String FACTORY_CONFIGURATION_PID = "my.factory.configuration.pid";
	public static final String NONSTATIC_CONFIGURATION_PID = "nonstatic.configuration.pid";

	@InjectService
	ConfigurationAdmin ca;

	@Test
	public void oldWay() throws Exception {

		Configuration c = ca.createFactoryConfiguration(FACTORY_CONFIGURATION_PID);
		c.update(Dictionaries.dictionaryOf("foo", "bar"));
		// test
		c.delete();

	}

	// START TESTS
	@InjectConfiguration(NONSTATIC_CONFIGURATION_PID)
	Configuration nonStaticConfiguration;

	@Test
	public void testFieldConfiguration() throws Exception {

		// in this Test the Framework is configured

		nonStaticConfiguration.update(Dictionaries.dictionaryOf("foo", "bar"));
		// no delete required

	}

	static final String PARAM_PID = "param.pid";

	@Test
	@WithConfiguration(pid = PARAM_PID, properties = { @Property(key = "bar", value = "foo") })
	public void testParameterConfiguration() throws Exception {

		// Test something
	}

	@Test
	@WithConfiguration(pid = PARAM_PID, properties = { @Property(key = "bar2", value = "foo2") })
	public void testParameterConfigurationInject(@InjectConfiguration(PARAM_PID) Configuration configuration) throws Exception {

		// Test something
		configuration.update(Dictionaries.dictionaryOf("f", "b"));

	}

	@Test
	public void test_Parameter_Dictionary(@InjectConfiguration(PARAM_PID) Dictionary<String, Object> dictionary) throws Exception {
	}

	@Test
	public void test_Parameter_Map(@InjectConfiguration(PARAM_PID) Map<String, Object> map) throws Exception {
	}

	@Test
	public void test_Parameter_Optional_NotNull(@InjectConfiguration(PARAM_PID) Optional<Configuration> cOptional) throws Exception {

	}

	@Test
	public void test_Parameter_Optional_Null(@InjectConfiguration("unknown.pid") Optional<Configuration> cOptional) throws Exception {

	}

	static final String METHOD_PID = "method.pid";

	@Test
	@WithConfiguration(pid = METHOD_PID, properties = { @Property(key = "foo", value = "bar") })
	public void testMethodConfiguration() throws Exception {

	}

	@Test
	@WithConfiguration(pid = FACTORY_CONFIGURATION_PID + "~" + "factory.name", properties = { @Property(key = "foo", value = "bar") })
	public void testMethodConfigurationFactory() throws Exception {

	}

	@Test
	@WithFactoryConfiguration(factoryPid = FACTORY_CONFIGURATION_PID, properties = { @Property(key = "foo", value = "bar") })
	@WithFactoryConfiguration(factoryPid = FACTORY_CONFIGURATION_PID, properties = { @Property(key = "fizz", value = "buzz") })
	public void testMethodConfigurationFactoryUnboundNames() throws Exception {

	}

	@Nested
	class SystemPropertyTests {

		private static final String ARRAY_NAME = "osgi.test.cm.array";
		private static final String METHOD_NAME = "osgi.test.cm.method.name";

		@BeforeEach
		void setConfig(TestInfo info) {
			System.setProperty(ARRAY_NAME, "1,2,3");
			info.getTestMethod().ifPresent(m -> System.setProperty(METHOD_NAME, m.getName()));
		}

		@AfterEach
		void unsetConfig() {
			System.clearProperty(ARRAY_NAME);
			System.clearProperty(METHOD_NAME);
		}

		// System Property Sources
		@Test
		@WithConfiguration(pid = "foo", properties = { @Property(key = "testScalar", value = METHOD_NAME, source = SystemProperty),
				@Property(key = "testArray", value = ARRAY_NAME, source = SystemProperty, type = PrimitiveArray, scalar = Byte),
				@Property(key = "testTemplate", value = { "Method : %s", "Easy As %2$s" }, type = Type.Collection, templateArguments = {
						@TemplateArgument(value = METHOD_NAME, source = SystemProperty), @TemplateArgument(value = ARRAY_NAME, source = SystemProperty) }) })
		void testAnnotated(@InjectService ConfigurationAdmin ca) throws Exception {
			Configuration cs = ConfigUtil.getConfigsByServicePid(ca, "foo");
			assertThat(cs).isNotNull();
			assertThat(cs.getProperties().get("testScalar")).isEqualTo("testAnnotated");
			assertArrayEquals(new byte[] { 1, 2, 3 }, (byte[]) cs.getProperties().get("testArray"));
			assertThat(cs.getProperties().get("testTemplate")).asList().containsExactly("Method : testAnnotated", "Easy As 1,2,3");
		}

		@Test
		void testInjected(
				@InjectConfiguration(withConfig = @WithConfiguration(pid = "foo", properties = { @Property(key = "testScalar", value = METHOD_NAME, source = SystemProperty),
						@Property(key = "testArray", value = ARRAY_NAME, source = SystemProperty, type = PrimitiveArray, scalar = Byte),
						@Property(key = "testTemplate", value = { "Method : %s", "Easy As %2$s" }, type = Type.Collection, templateArguments = {
								@TemplateArgument(value = METHOD_NAME, source = SystemProperty), @TemplateArgument(value = ARRAY_NAME, source = SystemProperty) }) })) Configuration cs)
				throws Exception {
			assertThat(cs).isNotNull();
			assertThat(cs.getProperties().get("testScalar")).isEqualTo("testInjected");
			assertArrayEquals(new byte[] { 1, 2, 3 }, (byte[]) cs.getProperties().get("testArray"));
			assertThat(cs.getProperties().get("testTemplate")).asList().containsExactly("Method : testInjected", "Easy As 1,2,3");
		}

		@Test
		void testFallback(@InjectConfiguration(withConfig = @WithConfiguration(pid = "foo", properties = { @Property(key = "testFallback", value = { "missing", "default" }, source = SystemProperty),
				@Property(key = "testTemplateFallback", value = { "Method : %s", "Easy As %2$s" }, type = Type.Collection, templateArguments = {
						@TemplateArgument(value = { "missing", "default2" }, source = SystemProperty),
						@TemplateArgument(value = { "missing", "default3" }, source = SystemProperty) }) })) Configuration cs)
				throws Exception {
			assertThat(cs).isNotNull();
			assertThat(cs.getProperties().get("testFallback")).isEqualTo("default");
			assertThat(cs.getProperties().get("testTemplateFallback")).asList().containsExactly("Method : default2", "Easy As default3");
		}

		@Test
		@WithConfiguration(pid = "foo", properties = { @Property(key = "testNumber", scalar = Scalar.Double, value = "%d.%.0f", templateArguments = {
				@TemplateArgument(value = { "missing", "5" }, source = SystemProperty, scalar = Scalar.Integer),
				@TemplateArgument(value = "java.specification.version", source = SystemProperty, scalar = Scalar.Double) }) })
		void testNumeric(@InjectService ConfigurationAdmin ca) throws Exception {
			Configuration cs = ConfigUtil.getConfigsByServicePid(ca, "foo");
			assertThat(cs).isNotNull();
			double javaVersion = Double.parseDouble(System.getProperty("java.specification.version"));
			assertThat(cs.getProperties().get("testNumber")).isEqualTo(Double.parseDouble("5." + Math.round(javaVersion)));
		}
	}

	@Nested
	class EnvironmentPropertyTests {

		// Environment Variable Sources
		@Test
		@WithConfiguration(pid = "foo", properties = { @Property(key = "testScalar", value = "PATH", source = EnvironmentVariable),
				@Property(key = "testTemplate", value = "Easy As %s", templateArguments = { @TemplateArgument(value = "PATH", source = EnvironmentVariable) }) })
		void testAnnotated(@InjectService ConfigurationAdmin ca) throws Exception {
			Configuration cs = ConfigUtil.getConfigsByServicePid(ca, "foo");
			assertThat(cs).isNotNull();
			assertThat(cs.getProperties().get("testScalar")).isEqualTo(System.getenv("PATH"));
			assertThat(cs.getProperties().get("testTemplate")).isEqualTo("Easy As " + System.getenv("PATH"));
		}

		@Test
		void testInjected(
				@InjectConfiguration(withConfig = @WithConfiguration(pid = "foo", properties = { @Property(key = "testScalar", value = "PATH", source = EnvironmentVariable),
						@Property(key = "testTemplate", value = "Easy As %s", templateArguments = { @TemplateArgument(value = "PATH", source = EnvironmentVariable) }) })) Configuration cs)
				throws Exception {
			assertThat(cs).isNotNull();
			assertThat(cs.getProperties().get("testScalar")).isEqualTo(System.getenv("PATH"));
			assertThat(cs.getProperties().get("testTemplate")).isEqualTo("Easy As " + System.getenv("PATH"));
		}

		@Test
		void testFallback(
				@InjectConfiguration(withConfig = @WithConfiguration(pid = "foo", properties = { @Property(key = "testFallback", value = { "missing", "default" }, source = EnvironmentVariable),
						@Property(key = "testTemplateFallback", value = { "Method : %s", "Easy As %2$s" }, type = Type.Collection, templateArguments = {
								@TemplateArgument(value = { "missing", "default2" }, source = EnvironmentVariable),
								@TemplateArgument(value = { "missing", "default3" }, source = EnvironmentVariable) }) })) Configuration cs)
				throws Exception {
			assertThat(cs).isNotNull();
			assertThat(cs.getProperties().get("testFallback")).isEqualTo("default");
			assertThat(cs.getProperties().get("testTemplateFallback")).asList().containsExactly("Method : default2", "Easy As default3");
		}
	}

	public static class TestIdAware implements BeforeTestExecutionCallback {
		@Override
		public void beforeTestExecution(ExtensionContext context) throws Exception {
			((TestPropertyTests) context.getRequiredTestInstance()).uniqueId = context.getUniqueId();
		}
	}

	@Nested
	@ExtendWith(TestIdAware.class)
	public class TestPropertyTests {

		String uniqueId;

		// Test Property Sources
		@Test
		@WithConfiguration(pid = "foo", properties = { @Property(key = "testName", source = TestClass), @Property(key = "testMethod", source = TestMethod),
				@Property(key = "testId", source = TestUniqueId), @Property(key = "testTemplate", value = { "Class : %s", "Test : %2$s", "Id : %3$s" }, type = Type.Collection, templateArguments = {
						@TemplateArgument(source = TestClass), @TemplateArgument(source = TestMethod), @TemplateArgument(source = TestUniqueId) }) })
		void testAnnotated(@InjectService ConfigurationAdmin ca) throws Exception {
			Configuration cs = ConfigUtil.getConfigsByServicePid(ca, "foo");
			assertThat(cs).isNotNull();
			assertThat(cs.getProperties().get("testName")).isEqualTo(TestPropertyTests.class.getName());
			assertThat(cs.getProperties().get("testMethod")).isEqualTo("testAnnotated");
			assertThat(cs.getProperties().get("testId")).isEqualTo(uniqueId);
			assertThat(cs.getProperties().get("testTemplate")).asList().containsExactly("Class : " + TestPropertyTests.class.getName(), "Test : testAnnotated", "Id : " + uniqueId);
		}

		@Test
		void testInjected(@InjectConfiguration(withConfig = @WithConfiguration(pid = "foo", properties = { @Property(key = "testName", source = TestClass),
				@Property(key = "testMethod", source = TestMethod), @Property(key = "testId", source = TestUniqueId),
				@Property(key = "testTemplate", value = { "Class : %s", "Test : %2$s", "Id : %3$s" }, type = Type.Collection, templateArguments = { @TemplateArgument(source = TestClass),
						@TemplateArgument(source = TestMethod), @TemplateArgument(source = TestUniqueId) }) })) Configuration cs)
				throws Exception {
			assertThat(cs).isNotNull();
			assertThat(cs.getProperties().get("testName")).isEqualTo(TestPropertyTests.class.getName());
			assertThat(cs.getProperties().get("testMethod")).isEqualTo("testInjected");
			assertThat(cs.getProperties().get("testId")).isEqualTo(uniqueId);
			assertThat(cs.getProperties().get("testTemplate")).asList().containsExactly("Class : " + TestPropertyTests.class.getName(), "Test : testInjected", "Id : " + uniqueId);
		}
	}
}

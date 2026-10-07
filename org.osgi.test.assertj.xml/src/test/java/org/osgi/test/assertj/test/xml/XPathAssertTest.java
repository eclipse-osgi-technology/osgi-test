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
package org.osgi.test.assertj.test.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.osgi.test.assertj.xml.XPathAssertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.assertj.xml.SimpleNamespaceContext;
import org.osgi.test.assertj.xml.XmlElement;
import org.w3c.dom.Document;

public class XPathAssertTest {

	static final String	SCR			= "http://www.osgi.org/xmlns/scr/v1.4.0";
	static final String	COMPONENT	= "<scr:component xmlns:scr=\"" + SCR + "\" name=\"my.component\" activate=\"start\">\n"
		+ "  <scr:implementation class=\"a.b.C\"/>\n"
		+ "  <scr:service><scr:provide interface=\"a.b.I\"/></scr:service>\n"
		+ "  <scr:reference name=\"r1\" interface=\"x.Y\"/>\n"
		+ "  <scr:reference name=\"r2\" interface=\"x.Z\" target=\"(a=b)\"/>\n"
		+ "  <scr:property name=\"list\" type=\"String\">\n    a\n    b\n  </scr:property>\n"
		+ "  <scr:property name=\"empty\">   </scr:property>\n"
		+ "</scr:component>";

	XmlElement			component;

	@BeforeEach
	void parse() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document document = factory.newDocumentBuilder()
			.parse(new ByteArrayInputStream(COMPONENT.getBytes(StandardCharsets.UTF_8)));
		component = new XmlElement("my.component", new SimpleNamespaceContext("scr", SCR),
			document.getDocumentElement());
	}

	@Test
	void passes() {
		assertThat(component).hasNamespace(SCR)
			.contains("scr:implementation")
			.doesNotContain("scr:nothing")
			.containsAnyOf("scr:nothing", "scr:service")
			.hasValue("@name", "my.component")
			.hasValue("scr:implementation/@class", "a.b.C")
			.hasOptionalValue("@activate", "start")
			.hasOptionalValue("@deactivate", "anything, there is no such attribute")
			.hasCount("scr:reference", 2)
			.hasCount("scr:reference[@target]", 1)
			.hasValuesExactlyInAnyOrder("scr:property[@name='list']/text()", "b", "a")
			.doesNotContainText("scr:property[@name='empty']")
			.doesNotContainText("scr:implementation");
	}

	@Test
	void failsWithTheElementId() {
		assertThatCode(() -> assertThat(component).contains("scr:nothing")).isInstanceOf(AssertionError.class)
			.hasMessageContaining("scr:nothing for node my.component");
		assertThatCode(() -> assertThat(component).hasValue("@name", "other")).isInstanceOf(AssertionError.class)
			.hasMessageContaining("@name for node my.component");
		assertThatCode(() -> assertThat(component).hasCount("scr:reference", 3)).isInstanceOf(AssertionError.class)
			.hasMessageContaining("count(scr:reference) for node my.component");
		assertThatCode(() -> assertThat(component).containsAnyOf("scr:a", "scr:b")).isInstanceOf(AssertionError.class)
			.hasMessageContaining("my.component");
		assertThatCode(() -> assertThat(component).doesNotContainText("scr:property[@name='list']"))
			.isInstanceOf(AssertionError.class);
		assertThatCode(() -> assertThat(component).hasNamespace("urn:other")).isInstanceOf(AssertionError.class);
	}

	@Test
	void rejectsInvalidExpressions() {
		assertThatCode(() -> assertThat(component).contains("scr:[")).isInstanceOf(AssertionError.class)
			.hasMessageContaining("invalid xpath expression");
	}

	@Test
	void domElementEntryPoint() {
		assertThat("my.component", new SimpleNamespaceContext("scr", SCR), component.getElement())
			.hasValue("@name", "my.component");
		assertThat(component.toString()).isEqualTo("XmlElement[my.component]");
	}

	@Test
	void namespaceContext() {
		SimpleNamespaceContext context = new SimpleNamespaceContext("scr", SCR);
		assertThat(context.getNamespaceURI("scr")).isEqualTo(SCR);
		assertThat(context.getNamespaceURI("xml")).isEqualTo("http://www.w3.org/XML/1998/namespace");
		assertThat(context.getNamespaceURI("other")).isEmpty();
		assertThat(context.getPrefix(SCR)).isEqualTo("scr");
		assertThat(context.getPrefixes(SCR)).toIterable()
			.containsExactly("scr");
		assertThatCode(() -> context.getNamespaceURI(null)).isInstanceOf(IllegalArgumentException.class);
	}
}

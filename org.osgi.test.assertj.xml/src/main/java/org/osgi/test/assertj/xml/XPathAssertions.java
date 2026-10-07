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
package org.osgi.test.assertj.xml;

import org.w3c.dom.Element;

/**
 * Entry point of the XML assertions.
 *
 * <pre>
 * import static org.osgi.test.assertj.xml.XPathAssertions.assertThat;
 *
 * SimpleNamespaceContext scr = new SimpleNamespaceContext("scr", "http://www.osgi.org/xmlns/scr/v1.4.0");
 * assertThat("my.component", scr, componentElement)
 * 	.hasNamespace("http://www.osgi.org/xmlns/scr/v1.4.0")
 * 	.hasValue("@name", "my.component")
 * 	.hasCount("scr:reference", 2)
 * 	.doesNotContain("scr:reference[@name='missing']");
 * </pre>
 */
public final class XPathAssertions {

	private XPathAssertions() {}

	/**
	 * Create an assertion for the element.
	 *
	 * @param actual the element under test
	 * @return the assertion
	 */
	public static XPathAssert assertThat(XmlElement actual) {
		return new XPathAssert(actual);
	}

	/**
	 * Create an assertion for a DOM element.
	 *
	 * @param id the name of the element in failure messages
	 * @param namespaceContext the namespace context of the XPath expressions
	 * @param element the element under test
	 * @return the assertion
	 */
	public static XPathAssert assertThat(String id, SimpleNamespaceContext namespaceContext, Element element) {
		return new XPathAssert(new XmlElement(id, namespaceContext, element));
	}
}

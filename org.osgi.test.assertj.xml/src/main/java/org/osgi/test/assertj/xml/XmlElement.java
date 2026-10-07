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

import java.util.Objects;

import org.w3c.dom.Element;

/**
 * The subject of an {@link XPathAssert}: a DOM element, the namespace context
 * its XPath expressions are evaluated in, and an id that names the element in
 * failure messages (a component name, a PID, ...).
 * <p>
 * Tests may extend it to carry what they know about the element.
 */
public class XmlElement {
	private final String					id;
	private final SimpleNamespaceContext	namespaceContext;
	private final Element					element;

	/**
	 * @param id the name of the element in failure messages
	 * @param namespaceContext the namespace context of the XPath expressions
	 * @param element the element
	 */
	public XmlElement(String id, SimpleNamespaceContext namespaceContext, Element element) {
		this.id = Objects.requireNonNull(id, "id");
		this.namespaceContext = Objects.requireNonNull(namespaceContext, "namespaceContext");
		this.element = Objects.requireNonNull(element, "element");
	}

	/**
	 * @return the name of the element in failure messages
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the element
	 */
	public Element getElement() {
		return element;
	}

	/**
	 * @return the namespace context of the XPath expressions
	 */
	public SimpleNamespaceContext getNamespaceContext() {
		return namespaceContext;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + id + "]";
	}
}

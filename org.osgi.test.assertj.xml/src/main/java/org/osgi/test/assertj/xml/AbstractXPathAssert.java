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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.assertj.core.api.AbstractAssert;
import org.w3c.dom.Node;

/**
 * Assertions on an XML element through XPath expressions, relative to the
 * element and evaluated in its namespace context.
 *
 * @param <SELF> the "self" type of this assertion class
 * @param <ACTUAL> the type of the element under test
 */
public abstract class AbstractXPathAssert<SELF extends AbstractXPathAssert<SELF, ACTUAL>, ACTUAL extends XmlElement>
	extends AbstractAssert<SELF, ACTUAL> {

	/**
	 * The XPath of this assertion, bound to the namespace context of the
	 * element.
	 */
	protected final XPath				xpath;

	protected AbstractXPathAssert(ACTUAL actual, Class<?> selfType) {
		super(actual, selfType);
		// XPathFactory and XPath are not thread safe: one of each per assertion
		xpath = XPathFactory.newInstance()
			.newXPath();
		if (actual != null) {
			xpath.setNamespaceContext(actual.getNamespaceContext());
		}
	}

	/**
	 * Verifies that the element's namespace is the given one.
	 *
	 * @param namespace the expected namespace URI
	 * @return this assertion object
	 */
	public SELF hasNamespace(String namespace) {
		isNotNull();
		assertThat(actual.getNamespaceContext()
			.getURI()).as("namespace for node %s", actual.getId())
				.isEqualTo(namespace);
		return myself;
	}

	/**
	 * Verifies that the expression selects a node.
	 *
	 * @param expr the XPath expression
	 * @return this assertion object
	 */
	public SELF contains(String expr) {
		isNotNull();
		assertThat(getNode(expr)).as("%s for node %s", expr, actual.getId())
			.isNotNull();
		return myself;
	}

	/**
	 * Verifies that the expression selects no node.
	 *
	 * @param expr the XPath expression
	 * @return this assertion object
	 */
	public SELF doesNotContain(String expr) {
		isNotNull();
		assertThat(getNode(expr)).as("%s for node %s", expr, actual.getId())
			.isNull();
		return myself;
	}

	/**
	 * Verifies that at least one of the expressions selects a node.
	 *
	 * @param expressions the XPath expressions
	 * @return this assertion object
	 */
	public SELF containsAnyOf(String... expressions) {
		isNotNull();
		for (String expr : expressions) {
			if (getNode(expr) != null) {
				return myself;
			}
		}
		failWithMessage("node %s did not match any of the expressions <'%s'>", actual.getId(),
			Arrays.toString(expressions));
		return myself;
	}

	/**
	 * Verifies that the expression selects a node with the given value.
	 *
	 * @param expr the XPath expression
	 * @param value the expected node value
	 * @return this assertion object
	 */
	public SELF hasValue(String expr, String value) {
		isNotNull();
		Node result = getNode(expr);
		assertThat(result).as("%s for node %s", expr, actual.getId())
			.isNotNull();
		assertThat(result.getNodeValue()).as("%s for node %s", expr, actual.getId())
			.isEqualTo(value);
		return myself;
	}

	/**
	 * Verifies that the expression selects a node whose white space separated
	 * value has exactly the given items, in any order.
	 *
	 * @param expr the XPath expression
	 * @param values the expected items
	 * @return this assertion object
	 */
	public SELF hasValuesExactlyInAnyOrder(String expr, String... values) {
		isNotNull();
		Node result = getNode(expr);
		assertThat(result).as("%s for node %s", expr, actual.getId())
			.isNotNull();
		assertThat(splitWhitespace(result.getNodeValue())).as("%s for node %s", expr, actual.getId())
			.containsExactlyInAnyOrder(values);
		return myself;
	}

	/**
	 * Verifies that the expression selects no node or a node with the given
	 * value.
	 *
	 * @param expr the XPath expression
	 * @param value the expected node value, if there is a node
	 * @return this assertion object
	 */
	public SELF hasOptionalValue(String expr, String value) {
		isNotNull();
		Node result = getNode(expr);
		if (result != null) {
			assertThat(result.getNodeValue()).as("%s for node %s", expr, actual.getId())
				.isEqualTo(value);
		}
		return myself;
	}

	/**
	 * Verifies that the expression selects the given number of nodes.
	 *
	 * @param expr the XPath expression
	 * @param count the expected count
	 * @return this assertion object
	 */
	public SELF hasCount(String expr, int count) {
		isNotNull();
		assertThat(getCount(expr)).as("count(%s) for node %s", expr, actual.getId())
			.isEqualTo(count);
		return myself;
	}

	/**
	 * Verifies that the node the expression selects has no text, or only
	 * white space.
	 *
	 * @param expr the XPath expression of the node
	 * @return this assertion object
	 */
	public SELF doesNotContainText(String expr) {
		isNotNull();
		Node result = getNode(expr + "/text()");
		if (result != null) {
			assertThat(result.getNodeValue()).as("%s/text() value for node %s", expr, actual.getId())
				.isBlank();
		}
		return myself;
	}

	/**
	 * The node the expression selects, relative to the element.
	 *
	 * @param expr the XPath expression
	 * @return the node, or {@code null} if the expression selects nothing
	 */
	public Node getNode(String expr) {
		try {
			return (Node) xpath.evaluate(expr, actual.getElement(), XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			failWithMessage("invalid xpath expression %s: %s", expr, e.getMessage());
			return null;
		}
	}

	/**
	 * The number of nodes the expression selects, relative to the element.
	 *
	 * @param expr the XPath expression
	 * @return the count
	 */
	public int getCount(String expr) {
		try {
			String result = (String) xpath.evaluate("count(" + expr + ")", actual.getElement(),
				XPathConstants.STRING);
			assertThat(result).as("count(%s) for node %s", expr, actual.getId())
				.containsOnlyDigits();
			return Integer.parseInt(result);
		} catch (XPathExpressionException e) {
			failWithMessage("invalid xpath expression %s: %s", expr, e.getMessage());
			return -1;
		}
	}

	private static List<String> splitWhitespace(String value) {
		if (value == null) {
			return Collections.emptyList();
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			return Collections.emptyList();
		}
		return Arrays.asList(trimmed.split("\\s+"));
	}
}

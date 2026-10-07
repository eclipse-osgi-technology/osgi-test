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

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Attr;

/**
 * A {@link NamespaceContext} of one prefix and its namespace URI, as the
 * XPath expressions of a test need it: {@code scr:component} with
 * {@code "scr"} bound to the SCR namespace, for example. The {@code xml} and
 * {@code xmlns} prefixes keep their standard meaning.
 */
public class SimpleNamespaceContext implements NamespaceContext {
	private final String	prefix;
	private final String	uri;

	/**
	 * @param prefix the prefix the XPath expressions use
	 * @param uri the namespace URI it stands for
	 */
	public SimpleNamespaceContext(String prefix, String uri) {
		this.prefix = Objects.requireNonNull(prefix, "prefix");
		this.uri = Objects.requireNonNull(uri, "uri");
	}

	/**
	 * The context of an {@code xmlns:prefix="uri"} attribute.
	 *
	 * @param namespaceAttribute the namespace declaration attribute
	 */
	public SimpleNamespaceContext(Attr namespaceAttribute) {
		this(namespaceAttribute.getLocalName(), namespaceAttribute.getValue());
	}

	/**
	 * @return the namespace URI
	 */
	public String getURI() {
		return uri;
	}

	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	@Override
	public String getNamespaceURI(String namespacePrefix) {
		if (namespacePrefix == null) {
			throw new IllegalArgumentException("prefix is null");
		}
		if (namespacePrefix.equals(prefix)) {
			return uri;
		}
		switch (namespacePrefix) {
			case XMLConstants.XML_NS_PREFIX :
				return XMLConstants.XML_NS_URI;
			case XMLConstants.XMLNS_ATTRIBUTE :
				return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
			default :
				return XMLConstants.NULL_NS_URI;
		}
	}

	@Override
	public String getPrefix(String namespaceURI) {
		if (namespaceURI == null) {
			throw new IllegalArgumentException("namespace URI is null");
		}
		if (namespaceURI.equals(uri)) {
			return prefix;
		}
		switch (namespaceURI) {
			case XMLConstants.XML_NS_URI :
				return XMLConstants.XML_NS_PREFIX;
			case XMLConstants.XMLNS_ATTRIBUTE_NS_URI :
				return XMLConstants.XMLNS_ATTRIBUTE;
			default :
				return null;
		}
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		String p = getPrefix(namespaceURI);
		return (p == null) ? Collections.<String> emptyIterator() : Collections.singletonList(p)
			.iterator();
	}

	@Override
	public String toString() {
		return "xmlns:" + prefix + "=\"" + uri + "\"";
	}
}

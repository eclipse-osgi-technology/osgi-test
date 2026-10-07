# org.osgi.test.assertj.xml

This artifact provides [AssertJ](https://github.com/assertj/assertj) assertions on XML
elements through XPath expressions, for checking generated descriptors such as
Declarative Services component descriptions or Metatype documents.

```java
import static org.osgi.test.assertj.xml.XPathAssertions.assertThat;

SimpleNamespaceContext scr = new SimpleNamespaceContext("scr", "http://www.osgi.org/xmlns/scr/v1.4.0");
XmlElement component = new XmlElement("my.component", scr, componentElement);

assertThat(component)
	.hasNamespace("http://www.osgi.org/xmlns/scr/v1.4.0")
	.hasValue("@name", "my.component")
	.hasOptionalValue("@activate", "activate")
	.contains("scr:implementation")
	.hasCount("scr:reference", 2)
	.hasValuesExactlyInAnyOrder("scr:property[@name='list']/text()", "a", "b")
	.doesNotContain("scr:reference[@name='missing']");
```

- The XPath expressions are relative to the element and use the prefix of the
  element's `SimpleNamespaceContext`.
- The element's id names it in failure messages.
- `XmlElement` can be extended to carry what a test knows about the element, and
  `AbstractXPathAssert` to add assertions for it.

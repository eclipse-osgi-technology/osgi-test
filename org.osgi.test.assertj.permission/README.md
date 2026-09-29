# org.osgi.test.assertj.permission

This artifact provides [AssertJ](https://github.com/assertj/assertj) assertions for
`java.security.Permission` and `java.security.PermissionCollection`.

```java
import static org.osgi.test.assertj.permission.PermissionAssertions.assertThat;

assertThat(new TopicPermission("a/b", "publish,subscribe"))
	.implies(new TopicPermission("a/b", "publish"))
	.doesNotImply(new TopicPermission("a/c", "publish"))
	.isEquivalentTo(new TopicPermission("a/b", "subscribe,publish"))
	.isSerializable();

assertThat(permission.newPermissionCollection())
	.accepts(p1, p2)
	.rejects(otherTypeOfPermission)
	.hasExactlyElements(p1, p2)
	.implies(p1)
	.isSerializable();
```

- `implies` / `doesNotImply` check every given permission and name the one that fails.
- `isEquivalentTo` checks `equals` in both directions and the hash code.
- `isSerializable` does a serialization round trip; classes are resolved through the class loader
  of the permission, so permission classes of other bundles work in an OSGi framework.
- `hasNoElements` / `hasElements` / `hasExactlyElements` enumerate `elements()` and also check the
  `Enumeration` contract (`nextElement()` throws `NoSuchElementException` at the end).
- `accepts` / `rejects` add permissions to a collection; `rejects` expects an
  `IllegalArgumentException` or a `SecurityException` (read-only collection).

`PermissionSoftAssertions` and `PermissionSoftAssertionsProvider` provide the soft assertion variants.

package org.osgi.test.assertj.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Filter;
import org.osgi.test.common.filter.Filters;

public class FilterConditionTest {

	Map<String, String> map;

	@BeforeEach
	void setUp() throws Exception {
		map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
	}

	@AfterEach
	void tearDown() throws Exception {}

	@Test
	void testIs() {
		Filter filter = Filters.format("(&(%s=%s)(%s=%s))", "key1", "value1", "key2", "value2");
		assertThat(filter).is(new FilterCondition(map));
	}

	@Test
	void testIsNot() {
		Filter filter = Filters.format("(&(%s=%s)(%s=%s))", "key1", "value1", "key2", "value1");
		assertThat(filter).isNot(new FilterCondition(map));
	}

}

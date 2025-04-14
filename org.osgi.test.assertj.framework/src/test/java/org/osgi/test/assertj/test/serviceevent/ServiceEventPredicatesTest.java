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

package org.osgi.test.assertj.test.serviceevent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Dictionary;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.test.assertj.serviceevent.ServiceEventPredicates;
import org.osgi.test.common.dictionary.Dictionaries;

public class ServiceEventPredicatesTest {
	ServiceReference<?> sr = Mockito.mock(ServiceReference.class);

	@Test
	void testPredicateService() throws Exception {

		SoftAssertions softly = new SoftAssertions();

		//

		Dictionary<String, Object> dict = Dictionaries.dictionaryOf(Constants.OBJECTCLASS, new String[] {
			A.class.getName()
		}, "key1", 1, "key2", 2);

		Mockito.when(sr.getPropertyKeys())
			.thenReturn(Collections.list(dict.keys())
				.toArray(new String[3]));

		Mockito.when(sr.getProperty(Mockito.any(String.class)))
			.then(new Answer<Object>() {

				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					String key = invocation.getArgument(0);
					return dict.get(key);
				}
			});

		ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, sr);

		//
		softly.assertThat(ServiceEventPredicates.type(ServiceEvent.REGISTERED)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.type(ServiceEvent.MODIFIED_ENDMATCH)
			.test(event))
			.isFalse();

		softly.assertThat(ServiceEventPredicates.containsServiceProperties(dict)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.containsServiceProperties(Dictionaries.asMap(dict))
			.test(event))
			.isTrue();


		softly.assertThat(ServiceEventPredicates.objectClass(A.class)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.matches(ServiceEvent.REGISTERED, A.class, dict)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.matches(ServiceEvent.REGISTERED, A.class, Dictionaries.dictionaryOf())
			.test(event))
			.isTrue();

		softly.assertAll();
	}

	interface A {}

	@Test
	void serviceEvent() throws Exception {
		assertThat(ServiceEventPredicates.serviceEvent()
			.test(new ServiceEvent(ServiceEvent.MODIFIED, sr))).isTrue();

		assertThat(ServiceEventPredicates.serviceEvent()
			.test("")).isFalse();

	}

}

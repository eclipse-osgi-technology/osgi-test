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
package org.osgi.test.assertj.test.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.test.assertj.bundleevent.BundleEventConditions;
import org.osgi.test.assertj.frameworkevent.FrameworkEventConditions;
import org.osgi.test.assertj.serviceevent.ServiceEventConditions;

public class EventConditionsTest {

	@Test
	public void bundleEventConditions() {
		Bundle bundle = mock(Bundle.class);
		Bundle origin = mock(Bundle.class);
		BundleEvent event = new BundleEvent(BundleEvent.STARTED, bundle, origin);

		assertThat(event).is(BundleEventConditions.type(BundleEvent.STARTED))
			.isNot(BundleEventConditions.type(BundleEvent.STOPPED))
			.is(BundleEventConditions.typeMaskedBy(BundleEvent.STARTED | BundleEvent.STOPPED))
			.isNot(BundleEventConditions.typeMaskedBy(BundleEvent.INSTALLED))
			.is(BundleEventConditions.bundle(bundle))
			.isNot(BundleEventConditions.bundle(origin))
			.is(BundleEventConditions.origin(origin));
		assertThat(BundleEventConditions.type(BundleEvent.STARTED)
			.description()
			.value()).contains("STARTED");
		assertThatThrownBy(() -> BundleEventConditions.type(BundleEvent.STARTED | BundleEvent.STOPPED))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> BundleEventConditions.type(0)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void serviceEventConditions() {
		ServiceReference<?> reference = mock(ServiceReference.class);
		ServiceReference<?> otherReference = mock(ServiceReference.class);
		ServiceEvent event = new ServiceEvent(ServiceEvent.MODIFIED, reference);

		assertThat(event).is(ServiceEventConditions.type(ServiceEvent.MODIFIED))
			.isNot(ServiceEventConditions.type(ServiceEvent.REGISTERED))
			.is(ServiceEventConditions.typeMaskedBy(ServiceEvent.MODIFIED | ServiceEvent.MODIFIED_ENDMATCH))
			.is(ServiceEventConditions.serviceReference(reference))
			.isNot(ServiceEventConditions.serviceReference(otherReference));
		assertThatThrownBy(() -> ServiceEventConditions.type(ServiceEvent.MODIFIED | ServiceEvent.REGISTERED))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void frameworkEventConditions() {
		Bundle bundle = mock(Bundle.class);
		FrameworkEvent event = new FrameworkEvent(FrameworkEvent.ERROR, bundle, new IllegalStateException("x"));

		assertThat(event).is(FrameworkEventConditions.type(FrameworkEvent.ERROR))
			.isNot(FrameworkEventConditions.type(FrameworkEvent.STARTED))
			.is(FrameworkEventConditions.typeMaskedBy(FrameworkEvent.ERROR | FrameworkEvent.WARNING))
			.is(FrameworkEventConditions.bundle(bundle))
			.is(FrameworkEventConditions.throwableOfType(RuntimeException.class))
			.isNot(FrameworkEventConditions.throwableOfType(java.io.IOException.class));
		FrameworkEvent noThrowable = new FrameworkEvent(FrameworkEvent.STARTED, bundle, null);
		assertThat(noThrowable).isNot(FrameworkEventConditions.throwableOfType(Throwable.class));
		assertThatThrownBy(() -> FrameworkEventConditions.type(FrameworkEvent.ERROR | FrameworkEvent.WARNING))
			.isInstanceOf(IllegalArgumentException.class);
	}
}

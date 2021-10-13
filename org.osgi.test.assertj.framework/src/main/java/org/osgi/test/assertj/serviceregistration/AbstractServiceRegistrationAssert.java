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

package org.osgi.test.assertj.serviceregistration;

import static org.osgi.test.assertj.servicereference.ServiceReferenceAssert.SERVICE_REFERENCE;

import org.assertj.core.api.AbstractObjectAssert;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.assertj.servicereference.AbstractServiceReferenceAssert;

public abstract class AbstractServiceRegistrationAssert<SELF extends AbstractServiceRegistrationAssert<SELF, ACTUAL, SERVICE>, ACTUAL extends ServiceRegistration<? extends SERVICE>, SERVICE>
	extends AbstractObjectAssert<SELF, ACTUAL> {

	protected AbstractServiceRegistrationAssert(ACTUAL actual, Class<?> selfType) {
		super(actual, selfType);
	}

	@SuppressWarnings("unchecked")
	public AbstractServiceReferenceAssert<?, ? extends ServiceReference<? extends SERVICE>, SERVICE> hasServiceReferenceThat() {
		return (AbstractServiceReferenceAssert<?, ? extends ServiceReference<? extends SERVICE>, SERVICE>) isNotNull()
			.extracting(ServiceRegistration::getReference, SERVICE_REFERENCE)
			.as(actual + ".serviceReference");
	}
}

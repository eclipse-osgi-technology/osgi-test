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
package org.osgi.test.common.event;

import java.util.EventObject;
import java.util.Objects;

import org.osgi.framework.AllServiceListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.test.common.bitmaps.Bitmap;

/**
 * Factory methods for {@link EventRecorder}s. Each recorder registers its
 * listener with the given {@link BundleContext} when it is created and removes
 * it when it is {@link EventRecorder#close() closed}.
 * <p>
 * Type masks use the {@code getType()} bit values of the respective event
 * class, combined with {@code |}. {@link #ALL_TYPES} matches every type.
 */
public final class EventRecorders {

	/**
	 * A type mask that matches every event type.
	 */
	public static final int ALL_TYPES = -1;

	private EventRecorders() {}

	/**
	 * Record all bundle events delivered to an asynchronous
	 * {@link BundleListener}. Such a listener never receives the
	 * {@code STARTING}, {@code STOPPING} and {@code LAZY_ACTIVATION} events.
	 *
	 * @param bundleContext the context to register the listener with
	 * @return a new, armed recorder
	 */
	public static EventRecorder<BundleEvent> bundleEvents(BundleContext bundleContext) {
		return bundleEvents(bundleContext, ALL_TYPES, false);
	}

	/**
	 * Record bundle events.
	 *
	 * @param bundleContext the context to register the listener with
	 * @param typeMask the {@link BundleEvent} types to record
	 * @param synchronous {@code true} to register a
	 *            {@link SynchronousBundleListener}, which also receives
	 *            {@code STARTING}, {@code STOPPING} and
	 *            {@code LAZY_ACTIVATION} and is called on the thread that
	 *            performs the bundle operation; {@code false} for an
	 *            asynchronous {@link BundleListener}
	 * @return a new, armed recorder
	 */
	public static EventRecorder<BundleEvent> bundleEvents(BundleContext bundleContext, int typeMask,
		boolean synchronous) {
		Objects.requireNonNull(bundleContext, "bundleContext");
		BundleEventRecorder recorder = synchronous ? new SynchronousBundleEventRecorder(bundleContext, typeMask)
			: new BundleEventRecorder(bundleContext, typeMask);
		bundleContext.addBundleListener(recorder);
		return recorder;
	}

	/**
	 * Record all service events visible to this bundle.
	 *
	 * @param bundleContext the context to register the listener with
	 * @return a new, armed recorder
	 */
	public static EventRecorder<ServiceEvent> serviceEvents(BundleContext bundleContext) {
		try {
			return serviceEvents(bundleContext, ALL_TYPES, null);
		} catch (InvalidSyntaxException e) {
			throw new IllegalStateException(e); // cannot happen without a filter
		}
	}

	/**
	 * Record service events through a {@link ServiceListener}, subject to the
	 * framework's package compatibility check.
	 *
	 * @param bundleContext the context to register the listener with
	 * @param typeMask the {@link ServiceEvent} types to record
	 * @param filter an LDAP filter on the service properties, or {@code null}
	 *            for all services
	 * @return a new, armed recorder
	 * @throws InvalidSyntaxException if the filter is malformed
	 */
	public static EventRecorder<ServiceEvent> serviceEvents(BundleContext bundleContext, int typeMask,
		String filter) throws InvalidSyntaxException {
		Objects.requireNonNull(bundleContext, "bundleContext");
		ServiceEventRecorder recorder = new ServiceEventRecorder(bundleContext, typeMask);
		bundleContext.addServiceListener(recorder, filter);
		return recorder;
	}

	/**
	 * Record service events through an {@link AllServiceListener}, which is
	 * exempt from the framework's package compatibility check.
	 *
	 * @param bundleContext the context to register the listener with
	 * @param typeMask the {@link ServiceEvent} types to record
	 * @param filter an LDAP filter on the service properties, or {@code null}
	 *            for all services
	 * @return a new, armed recorder
	 * @throws InvalidSyntaxException if the filter is malformed
	 */
	public static EventRecorder<ServiceEvent> allServiceEvents(BundleContext bundleContext, int typeMask,
		String filter) throws InvalidSyntaxException {
		Objects.requireNonNull(bundleContext, "bundleContext");
		AllServiceEventRecorder recorder = new AllServiceEventRecorder(bundleContext, typeMask);
		bundleContext.addServiceListener(recorder, filter);
		return recorder;
	}

	/**
	 * Record all framework events.
	 *
	 * @param bundleContext the context to register the listener with
	 * @return a new, armed recorder
	 */
	public static EventRecorder<FrameworkEvent> frameworkEvents(BundleContext bundleContext) {
		return frameworkEvents(bundleContext, ALL_TYPES);
	}

	/**
	 * Record framework events.
	 *
	 * @param bundleContext the context to register the listener with
	 * @param typeMask the {@link FrameworkEvent} types to record
	 * @return a new, armed recorder
	 */
	public static EventRecorder<FrameworkEvent> frameworkEvents(BundleContext bundleContext, int typeMask) {
		Objects.requireNonNull(bundleContext, "bundleContext");
		FrameworkEventRecorder recorder = new FrameworkEventRecorder(bundleContext, typeMask);
		bundleContext.addFrameworkListener(recorder);
		return recorder;
	}

	/**
	 * Record bundle events (synchronously), service events (through an
	 * {@link AllServiceListener}) and framework events in one recorder, so
	 * that their relative order is preserved.
	 *
	 * @param bundleContext the context to register the listeners with
	 * @return a new, armed recorder
	 */
	public static EventRecorder<EventObject> allEvents(BundleContext bundleContext) {
		return compositeEvents(bundleContext, true);
	}

	/**
	 * Record bundle events (synchronously) and service events (through an
	 * {@link AllServiceListener}) in one recorder, so that their relative
	 * order is preserved.
	 *
	 * @param bundleContext the context to register the listeners with
	 * @return a new, armed recorder
	 */
	public static EventRecorder<EventObject> bundleAndServiceEvents(BundleContext bundleContext) {
		return compositeEvents(bundleContext, false);
	}

	/**
	 * Create a recorder for the given event class. This is the entry point for
	 * the JUnit integrations, which take the event class from the generic type
	 * of the injection target.
	 *
	 * @param bundleContext the context to register the listener with
	 * @param eventType {@link BundleEvent}, {@link ServiceEvent},
	 *            {@link FrameworkEvent}, or {@link EventObject} for
	 *            {@link #allEvents(BundleContext)}
	 * @param typeMask the event types to record; ignored for
	 *            {@code EventObject}
	 * @param synchronous for bundle events, whether to register a
	 *            {@link SynchronousBundleListener}; must be {@code false} for
	 *            service and framework events
	 * @param allServices for service events, whether to register an
	 *            {@link AllServiceListener}; must be {@code false} for bundle
	 *            and framework events
	 * @param filter for service events, an LDAP filter or {@code null}; must
	 *            be {@code null} for bundle and framework events
	 * @return a new, armed recorder
	 * @throws InvalidSyntaxException if the filter is malformed
	 * @throws IllegalArgumentException if the event class is not supported or
	 *             an option does not apply to it
	 */
	public static EventRecorder<? extends EventObject> create(BundleContext bundleContext,
		Class<? extends EventObject> eventType, int typeMask, boolean synchronous, boolean allServices,
		String filter) throws InvalidSyntaxException {
		Objects.requireNonNull(eventType, "eventType");
		if (eventType == BundleEvent.class) {
			requireNotSet(allServices, "allServices", eventType);
			requireNotSet(filter != null, "filter", eventType);
			return bundleEvents(bundleContext, typeMask, synchronous);
		}
		if (eventType == ServiceEvent.class) {
			requireNotSet(synchronous, "synchronous", eventType);
			return allServices ? allServiceEvents(bundleContext, typeMask, filter)
				: serviceEvents(bundleContext, typeMask, filter);
		}
		if (eventType == FrameworkEvent.class) {
			requireNotSet(synchronous, "synchronous", eventType);
			requireNotSet(allServices, "allServices", eventType);
			requireNotSet(filter != null, "filter", eventType);
			return frameworkEvents(bundleContext, typeMask);
		}
		if (eventType == EventObject.class) {
			requireNotSet(synchronous, "synchronous", eventType);
			requireNotSet(allServices, "allServices", eventType);
			requireNotSet(filter != null, "filter", eventType);
			return allEvents(bundleContext);
		}
		throw new IllegalArgumentException("Unsupported event type " + eventType.getName()
			+ ". Supported are BundleEvent, ServiceEvent, FrameworkEvent and EventObject (all events).");
	}

	private static void requireNotSet(boolean set, String option, Class<?> eventType) {
		if (set) {
			throw new IllegalArgumentException(
				"The option '" + option + "' does not apply to " + eventType.getSimpleName() + " recorders");
		}
	}

	private static EventRecorder<EventObject> compositeEvents(BundleContext bundleContext,
		boolean frameworkEvents) {
		Objects.requireNonNull(bundleContext, "bundleContext");
		CompositeEventRecorder recorder = new CompositeEventRecorder(bundleContext, frameworkEvents);
		bundleContext.addBundleListener(recorder);
		bundleContext.addServiceListener(recorder);
		if (frameworkEvents) {
			bundleContext.addFrameworkListener(recorder);
		}
		return recorder;
	}

	static class BundleEventRecorder extends AbstractEventRecorder<BundleEvent> implements BundleListener {
		private final BundleContext	bundleContext;
		private final int			typeMask;

		BundleEventRecorder(BundleContext bundleContext, int typeMask) {
			this.bundleContext = bundleContext;
			this.typeMask = typeMask;
		}

		@Override
		public void bundleChanged(BundleEvent event) {
			if (Bitmap.typeMatchesMask(event.getType(), typeMask)) {
				record(event);
			}
		}

		@Override
		protected void unregister() {
			bundleContext.removeBundleListener(this);
		}
	}

	static final class SynchronousBundleEventRecorder extends BundleEventRecorder
		implements SynchronousBundleListener {
		SynchronousBundleEventRecorder(BundleContext bundleContext, int typeMask) {
			super(bundleContext, typeMask);
		}
	}

	static class ServiceEventRecorder extends AbstractEventRecorder<ServiceEvent> implements ServiceListener {
		private final BundleContext	bundleContext;
		private final int			typeMask;

		ServiceEventRecorder(BundleContext bundleContext, int typeMask) {
			this.bundleContext = bundleContext;
			this.typeMask = typeMask;
		}

		@Override
		public void serviceChanged(ServiceEvent event) {
			if (Bitmap.typeMatchesMask(event.getType(), typeMask)) {
				record(event);
			}
		}

		@Override
		protected void unregister() {
			bundleContext.removeServiceListener(this);
		}
	}

	static final class AllServiceEventRecorder extends ServiceEventRecorder implements AllServiceListener {
		AllServiceEventRecorder(BundleContext bundleContext, int typeMask) {
			super(bundleContext, typeMask);
		}
	}

	static final class FrameworkEventRecorder extends AbstractEventRecorder<FrameworkEvent>
		implements FrameworkListener {
		private final BundleContext	bundleContext;
		private final int			typeMask;

		FrameworkEventRecorder(BundleContext bundleContext, int typeMask) {
			this.bundleContext = bundleContext;
			this.typeMask = typeMask;
		}

		@Override
		public void frameworkEvent(FrameworkEvent event) {
			if (Bitmap.typeMatchesMask(event.getType(), typeMask)) {
				record(event);
			}
		}

		@Override
		protected void unregister() {
			bundleContext.removeFrameworkListener(this);
		}
	}

	static final class CompositeEventRecorder extends AbstractEventRecorder<EventObject>
		implements SynchronousBundleListener, AllServiceListener, FrameworkListener {
		private final BundleContext	bundleContext;
		private final boolean		frameworkEvents;

		CompositeEventRecorder(BundleContext bundleContext, boolean frameworkEvents) {
			this.bundleContext = bundleContext;
			this.frameworkEvents = frameworkEvents;
		}

		@Override
		public void bundleChanged(BundleEvent event) {
			record(event);
		}

		@Override
		public void serviceChanged(ServiceEvent event) {
			record(event);
		}

		@Override
		public void frameworkEvent(FrameworkEvent event) {
			record(event);
		}

		@Override
		protected void unregister() {
			bundleContext.removeBundleListener(this);
			bundleContext.removeServiceListener(this);
			if (frameworkEvents) {
				bundleContext.removeFrameworkListener(this);
			}
		}
	}
}

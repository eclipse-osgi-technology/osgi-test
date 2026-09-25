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
package org.osgi.test.common.test.event;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.osgi.framework.BundleEvent.STARTED;
import static org.osgi.framework.BundleEvent.STARTING;
import static org.osgi.framework.BundleEvent.STOPPED;
import static org.osgi.framework.BundleEvent.STOPPING;
import static org.osgi.test.common.event.EventRecorders.ALL_TYPES;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.test.common.event.TimedEvent;
import org.osgi.test.common.event.EventRecorder;
import org.osgi.test.common.event.EventRecorders;
import org.osgi.test.common.event.EventTimeoutException;

public class EventRecorderTest {

	BundleContext					ctx;
	Bundle							bundle;
	private final List<Thread>		noisyThreads	= new ArrayList<>();
	private final List<EventRecorder<?>>	recorders		= new ArrayList<>();

	@BeforeEach
	void setup() throws Exception {
		ctx = FrameworkUtil.getBundle(getClass())
			.getBundleContext();
		Manifest manifest = new Manifest();
		manifest.getMainAttributes()
			.putValue(Constants.BUNDLE_MANIFESTVERSION, "2");
		manifest.getMainAttributes()
			.putValue(Constants.BUNDLE_SYMBOLICNAME, "recorderTestBundle");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JarOutputStream jos = new JarOutputStream(baos, manifest)) {}
		bundle = ctx.installBundle("recorderTest", new ByteArrayInputStream(baos.toByteArray()));
		bundle.start();
	}

	@AfterEach
	void cleanup() throws Exception {
		noisyThreads.forEach(t -> {
			t.interrupt();
			try {
				t.join(100);
			} catch (InterruptedException e) {}
		});
		noisyThreads.clear();
		recorders.forEach(EventRecorder::close);
		recorders.clear();
		bundle.uninstall();
	}

	private <E extends EventObject> EventRecorder<E> track(EventRecorder<E> recorder) {
		recorders.add(recorder);
		return recorder;
	}

	private void restart() throws Exception {
		bundle.stop();
		bundle.start();
	}

	private void restartLater(int iterations, int delay) {
		Thread t = new Thread(() -> {
			for (int i = 0; i < iterations; i++) {
				try {
					Thread.sleep(delay);
					restart();
				} catch (InterruptedException e) {
					return;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		noisyThreads.add(t);
		t.start();
	}

	private static Predicate<BundleEvent> type(int type) {
		return e -> e.getType() == type;
	}

	private static List<Integer> types(List<? extends TimedEvent<? extends EventObject>> events) {
		List<Integer> result = new ArrayList<>();
		for (TimedEvent<? extends EventObject> e : events) {
			EventObject event = e.event();
			if (event instanceof BundleEvent) {
				result.add(((BundleEvent) event).getType());
			} else if (event instanceof ServiceEvent) {
				result.add(((ServiceEvent) event).getType());
			} else if (event instanceof FrameworkEvent) {
				result.add(((FrameworkEvent) event).getType());
			}
		}
		return result;
	}

	@Test
	public void synchronousRecorderSeesAllTypesWithoutWaiting() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		restart();
		// Synchronous listeners have been called before stop()/start() return
		assertThat(types(r.events())).containsExactly(STOPPING, STOPPED, STARTING, STARTED);
		assertThat(types(r.waitForCount(4, Duration.ZERO))).containsExactly(STOPPING, STOPPED, STARTING, STARTED);
	}

	@Test
	public void asynchronousRecorderNeverSeesStartingAndStopping() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx));
		restart();
		List<TimedEvent<BundleEvent>> events = r.waitForCountThenQuiet(2, ofMillis(200), ofSeconds(5));
		assertThat(types(events)).containsExactly(STOPPED, STARTED);
	}

	@Test
	public void typeMaskFiltersEvents() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, STARTING | STARTED, true));
		restart();
		assertThat(types(r.drain())).containsExactly(STARTING, STARTED);
	}

	@Test
	public void waitForCountReturnsMoreIfMoreArrived() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		restart();
		assertThat(r.waitForCount(2, ofSeconds(1))).hasSize(4);
		assertThat(r.size()).isZero();
	}

	@Test
	public void waitForCountTimesOutWithRecordedEvents() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		bundle.stop();
		long start = System.nanoTime();
		assertThatThrownBy(() -> r.waitForCount(3, ofMillis(200))).isInstanceOf(EventTimeoutException.class)
			.satisfies(t -> {
				EventTimeoutException e = (EventTimeoutException) t;
				assertThat(e.getTimeout()).isEqualTo(ofMillis(200));
				assertThat(e.getExpectation()).contains("3");
				assertThat(e.getEvents()).hasSize(2);
			});
		assertThat(Duration.ofNanos(System.nanoTime() - start)).isGreaterThanOrEqualTo(ofMillis(200));
		// A timeout does not drain
		assertThat(r.size()).isEqualTo(2);
	}

	@Test
	public void waitForZeroEventsReturnsImmediately() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx));
		assertThat(r.waitForCount(0, ofSeconds(5))).isEmpty();
	}

	@Test
	public void waitForPredicateDrainsUpToTheMatch() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		restart();
		List<TimedEvent<BundleEvent>> events = r.waitFor(type(STARTING), ofSeconds(1));
		assertThat(types(events)).containsExactly(STOPPING, STOPPED, STARTING);
		assertThat(types(r.events())).containsExactly(STARTED);
	}

	@Test
	public void waitForPredicateWaitsForLaterEvents() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		restartLater(1, 100);
		List<TimedEvent<BundleEvent>> events = r.waitFor(type(STARTED), ofSeconds(5));
		assertThat(types(events)).containsExactly(STOPPING, STOPPED, STARTING, STARTED);
	}

	@Test
	public void waitForPredicateTimesOut() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		bundle.stop();
		assertThatThrownBy(() -> r.waitFor(type(STARTED), ofMillis(100))).isInstanceOf(EventTimeoutException.class)
			.hasMessageContaining("Recorded 2 event(s)");
	}

	@Test
	public void waitForSequenceAllowsGaps() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		restart();
		restart();
		List<TimedEvent<BundleEvent>> events = r
			.waitForSequence(Arrays.asList(type(STOPPED), type(STARTED), type(STOPPED)), ofSeconds(1));
		assertThat(types(events)).containsExactly(STOPPING, STOPPED, STARTING, STARTED, STOPPING, STOPPED);
		assertThat(types(r.events())).containsExactly(STARTING, STARTED);
	}

	@Test
	public void waitForSequenceRequiresOrder() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		restart();
		assertThatThrownBy(() -> r.waitForSequence(Arrays.asList(type(STARTED), type(STOPPED)), ofMillis(100)))
			.isInstanceOf(EventTimeoutException.class)
			.hasMessageContaining("1 of which occurred");
		assertThat(r.size()).isEqualTo(4);
	}

	@Test
	public void waitForEmptySequenceReturnsImmediately() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		restart();
		assertThat(r.waitForSequence(new ArrayList<Predicate<BundleEvent>>(), ofSeconds(1))).isEmpty();
		assertThat(r.size()).isEqualTo(4);
	}

	@Test
	public void waitForQuietWaitsForTheNoiseToStop() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx));
		restartLater(3, 100);
		long start = System.nanoTime();
		List<TimedEvent<BundleEvent>> events = r.waitForQuiet(ofMillis(400), ofSeconds(5));
		Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
		assertThat(events).hasSize(6);
		assertThat(events).isSorted();
		assertThat(events).allSatisfy(e -> assertThat(e.time()).isBetween(Duration.ZERO, elapsed));
		assertThat(elapsed).isGreaterThanOrEqualTo(ofMillis(700));
	}

	@Test
	public void waitForQuietReturnsAtOnceWhenAlreadyQuiet() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		restart();
		Thread.sleep(300);
		long start = System.nanoTime();
		assertThat(r.waitForQuiet(ofMillis(200), ofSeconds(5))).hasSize(4);
		assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(ofMillis(150));
	}

	@Test
	public void waitForQuietGivesUpEarlyWhenTheQuietPeriodCannotFit() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx));
		restartLater(10, 100);
		assertThatThrownBy(() -> r.waitForQuiet(ofMillis(400), ofSeconds(1))).isInstanceOf(EventTimeoutException.class)
			.hasMessageContaining("quiet");
	}

	@Test
	public void waitForQuietValidatesArguments() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx));
		assertThatThrownBy(() -> r.waitForQuiet(Duration.ZERO, ofSeconds(1))).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> r.waitForQuiet(ofSeconds(1), ofSeconds(1))).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> r.waitForCount(-1, ofSeconds(1))).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void waitForCountThenQuietCollectsTheWholeBatch() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		restartLater(2, 100);
		List<TimedEvent<BundleEvent>> events = r.waitForCountThenQuiet(4, ofMillis(300), ofSeconds(5));
		// The second restart happens within the quiet period of the first
		assertThat(events).hasSize(8);
		assertThat(r.size()).isZero();
	}

	@Test
	public void waitForCountThenQuietTimesOutOnCount() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		assertThatThrownBy(() -> r.waitForCountThenQuiet(1, ofMillis(50), ofMillis(200)))
			.isInstanceOf(EventTimeoutException.class)
			.hasMessageContaining("at least 1 event(s)");
	}

	@Test
	public void collectReturnsWhatIsThereOnTimeout() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		bundle.stop();
		long start = System.nanoTime();
		// Only 2 events: returns them after the timeout instead of failing
		assertThat(types(r.collect(4, ofMillis(200)))).containsExactly(STOPPING, STOPPED);
		assertThat(Duration.ofNanos(System.nanoTime() - start)).isGreaterThanOrEqualTo(ofMillis(200));
		assertThat(r.size()).isZero();
		bundle.start();
		// Enough events: returns at once
		start = System.nanoTime();
		assertThat(types(r.collect(2, ofSeconds(5)))).containsExactly(STARTING, STARTED);
		assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(ofSeconds(1));
	}

	@Test
	public void collectQuietBehavesLikeGetResults() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		// A test may branch on the number of events, so 4 requested and 2
		// recorded is not a failure
		bundle.stop();
		assertThat(r.collectQuiet(4, ofMillis(200), ofMillis(500))).hasSize(2);
		// The whole batch, including events after the minimum count, as long as
		// they arrive within the quiet period: the bundle is stopped, so the
		// first restart only yields STARTING and STARTED, the second all four
		restartLater(2, 100);
		assertThat(types(r.collectQuiet(2, ofMillis(300), ofSeconds(5)))).containsExactly(STARTING, STARTED, STOPPING,
			STOPPED, STARTING, STARTED);
		// Nothing happening: returns empty after the quiet period, not the timeout
		long start = System.nanoTime();
		assertThat(r.collectQuiet(0, ofMillis(100), ofSeconds(5))).isEmpty();
		assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(ofSeconds(1));
	}

	@Test
	public void giveUpWhenQuietFailsFast() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		bundle.stop();
		long start = System.nanoTime();
		// STARTED never comes; the framework is quiet after STOPPING, STOPPED
		assertThatThrownBy(() -> r.waitFor(type(STARTED), ofSeconds(10), ofMillis(200)))
			.isInstanceOf(EventTimeoutException.class)
			.hasMessageContaining("gave up")
			.hasMessageContaining("Recorded 2 event(s)");
		assertThat(Duration.ofNanos(System.nanoTime() - start)).isBetween(ofMillis(200), ofSeconds(2));
		assertThatThrownBy(() -> r.waitForCount(3, ofSeconds(10), ofMillis(200)))
			.isInstanceOf(EventTimeoutException.class)
			.hasMessageContaining("gave up");
		assertThatThrownBy(() -> r.waitForSequence(Arrays.asList(type(STOPPED), type(STARTED)), ofSeconds(10),
			ofMillis(200))).isInstanceOf(EventTimeoutException.class)
				.hasMessageContaining("1 of which occurred")
				.hasMessageContaining("gave up");
		assertThatThrownBy(() -> r.waitFor(type(STARTED), ofSeconds(1), Duration.ZERO))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void giveUpWhenQuietIsMeasuredFromTheStartOfTheWait() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		// The recorder has been idle for longer than the give-up period, but
		// the wait must still allow the action to produce its first event
		Thread.sleep(300);
		restartLater(1, 100);
		List<TimedEvent<BundleEvent>> events = r.waitFor(type(STARTED), ofSeconds(5), ofMillis(250));
		assertThat(types(events)).containsExactly(STOPPING, STOPPED, STARTING, STARTED);
		// Events keep the wait alive as long as they arrive within the period
		restartLater(3, 150);
		assertThat(r.waitForCount(12, ofSeconds(5), ofMillis(400))).hasSize(12);
	}

	@Test
	public void eventsDoesNotDrainButDrainDoes() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		restart();
		assertThat(r.events()).hasSize(4);
		assertThat(r.events()).hasSize(4);
		assertThat(r.size()).isEqualTo(4);
		assertThat(r.drain()).hasSize(4);
		assertThat(r.size()).isZero();
		assertThat(r.events()).isEmpty();
		restart();
		r.clear();
		assertThat(r.size()).isZero();
	}

	@Test
	public void timesAreRelativeToTheCreationOfTheRecorder() throws Exception {
		EventRecorder<BundleEvent> r = track(EventRecorders.bundleEvents(ctx, ALL_TYPES, true));
		Thread.sleep(100);
		restart();
		Thread.sleep(100);
		restart();
		Duration elapsed = ofMillis(400);
		List<TimedEvent<BundleEvent>> events = r.drain();
		assertThat(events).hasSize(8);
		assertThat(events).isSorted();
		assertThat(events.get(0)
			.time()).isGreaterThanOrEqualTo(ofMillis(100));
		assertThat(events.get(4)
			.time()).isGreaterThanOrEqualTo(ofMillis(200));
		assertThat(events).allSatisfy(e -> assertThat(e.time()).isLessThan(elapsed));
	}

	@Test
	public void closeRemovesTheListenerAndWakesWaiters() throws Exception {
		EventRecorder<BundleEvent> r = EventRecorders.bundleEvents(ctx, ALL_TYPES, true);
		Thread waiter = new Thread(() -> {
			try {
				r.waitForCount(1, ofSeconds(10));
			} catch (IllegalStateException expected) {
				return;
			} catch (InterruptedException e) {
				return;
			}
		});
		waiter.start();
		Thread.sleep(50);
		r.close();
		waiter.join(1000);
		assertThat(waiter.isAlive()).isFalse();
		assertThat(r.isClosed()).isTrue();
		restart();
		assertThat(r.size()).isZero();
		assertThatThrownBy(() -> r.waitForCount(1, ofSeconds(1))).isInstanceOf(IllegalStateException.class);
		// closing twice is fine
		r.close();
	}

	@Test
	public void serviceEventsWithFilter() throws Exception {
		EventRecorder<ServiceEvent> r = track(EventRecorders.serviceEvents(ctx, ALL_TYPES, "(count>=3)"));
		ServiceRegistration<Object> reg = ctx.registerService(Object.class, new Object(), null);
		try {
			for (int i = 1; i <= 5; i++) {
				Hashtable<String, Object> props = new Hashtable<>();
				props.put("count", i);
				reg.setProperties(props);
			}
			// Service events are synchronous: no waiting required
			assertThat(types(r.events())).containsExactly(ServiceEvent.MODIFIED, ServiceEvent.MODIFIED,
				ServiceEvent.MODIFIED);
			assertThat(r.events()).allSatisfy(e -> assertThat(e.event()
				.getServiceReference()).isEqualTo(reg.getReference()));
		} finally {
			r.close();
			reg.unregister();
		}
	}

	@Test
	public void serviceEventsRejectBadFilter() {
		assertThatThrownBy(() -> EventRecorders.serviceEvents(ctx, ALL_TYPES, "(count"))
			.isInstanceOf(org.osgi.framework.InvalidSyntaxException.class);
	}

	@Test
	public void frameworkEvents() throws Exception {
		EventRecorder<FrameworkEvent> r = track(
			EventRecorders.frameworkEvents(ctx, FrameworkEvent.STARTLEVEL_CHANGED));
		FrameworkStartLevel fsl = ctx.getBundle(0)
			.adapt(FrameworkStartLevel.class);
		// Setting the current start level again still fires STARTLEVEL_CHANGED
		fsl.setStartLevel(fsl.getStartLevel());
		List<TimedEvent<FrameworkEvent>> events = r.waitForCount(1, ofSeconds(5));
		assertThat(types(events)).containsExactly(FrameworkEvent.STARTLEVEL_CHANGED);
	}

	@Test
	public void createChoosesTheRecorderByEventClass() throws Exception {
		assertThat(track(EventRecorders.create(ctx, BundleEvent.class, ALL_TYPES, true, false, null)))
			.isInstanceOf(org.osgi.framework.SynchronousBundleListener.class);
		assertThat(track(EventRecorders.create(ctx, BundleEvent.class, ALL_TYPES, false, false, null)))
			.isInstanceOf(org.osgi.framework.BundleListener.class)
			.isNotInstanceOf(org.osgi.framework.SynchronousBundleListener.class);
		assertThat(track(EventRecorders.create(ctx, ServiceEvent.class, ALL_TYPES, false, true, "(a=b)")))
			.isInstanceOf(org.osgi.framework.AllServiceListener.class);
		assertThat(track(EventRecorders.create(ctx, ServiceEvent.class, ALL_TYPES, false, false, null)))
			.isInstanceOf(org.osgi.framework.ServiceListener.class)
			.isNotInstanceOf(org.osgi.framework.AllServiceListener.class);
		assertThat(track(EventRecorders.create(ctx, FrameworkEvent.class, ALL_TYPES, false, false, null)))
			.isInstanceOf(org.osgi.framework.FrameworkListener.class);
		assertThat(track(EventRecorders.create(ctx, EventObject.class, ALL_TYPES, false, false, null)))
			.isInstanceOf(org.osgi.framework.FrameworkListener.class)
			.isInstanceOf(org.osgi.framework.SynchronousBundleListener.class)
			.isInstanceOf(org.osgi.framework.AllServiceListener.class);
	}

	@Test
	public void createRejectsOptionsThatDoNotApply() {
		assertThatThrownBy(() -> EventRecorders.create(ctx, ServiceEvent.class, ALL_TYPES, true, false, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("synchronous");
		assertThatThrownBy(() -> EventRecorders.create(ctx, BundleEvent.class, ALL_TYPES, false, false, "(a=b)"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("filter");
		assertThatThrownBy(() -> EventRecorders.create(ctx, FrameworkEvent.class, ALL_TYPES, false, true, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("allServices");
		assertThatThrownBy(() -> EventRecorders.create(ctx, UnknownEvent.class, ALL_TYPES, false, false, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("UnknownEvent");
	}

	static class UnknownEvent extends EventObject {
		private static final long serialVersionUID = 1L;

		UnknownEvent() {
			super("x");
		}
	}

	@Test
	public void allEventsKeepTheirRelativeOrder() throws Exception {
		EventRecorder<EventObject> r = track(EventRecorders.allEvents(ctx));
		ServiceRegistration<Object> reg = ctx.registerService(Object.class, new Object(), null);
		restart();
		reg.unregister();
		List<TimedEvent<EventObject>> events = r.drain();
		assertThat(events).hasSize(6);
		assertThat(events.get(0)
			.event()).isInstanceOf(ServiceEvent.class);
		assertThat(types(events.subList(1, 5))).containsExactly(STOPPING, STOPPED, STARTING, STARTED);
		assertThat(events.get(5)
			.event()).isInstanceOf(ServiceEvent.class);
	}
}

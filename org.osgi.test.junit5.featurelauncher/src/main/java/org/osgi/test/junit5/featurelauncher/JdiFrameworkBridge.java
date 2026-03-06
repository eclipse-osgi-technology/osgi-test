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

package org.osgi.test.junit5.featurelauncher;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.osgi.dto.DTO;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.framework.startlevel.dto.FrameworkStartLevelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

/**
 * Extracts OSGi DTOs via the standard OSGi API using JDI method invocation.
 * <p>
 * Uses the chain: {@code framework.adapt(FrameworkDTO.class)} to get complete
 * DTOs including services and properties — framework-independent.
 * <p>
 * Requires an event-suspended thread for {@code invokeMethod()}, which is
 * acquired via a StepRequest on a suitable thread.
 */
class JdiFrameworkBridge {

	private static final Logger LOG = LoggerFactory.getLogger(JdiFrameworkBridge.class);

	private static final String THREAD_PREFIX_JDI = "JDI ";
	private static final String THREAD_NAME_MAIN = "main";
	private static final String NATIVE_METHOD_WAIT = "wait0";
	private static final String NATIVE_METHOD_PARK = "park";
	private final VirtualMachine vm;

	/**
	 * Tracks outgoing DTOs and their remote VM ObjectReferences. When a DTO is read
	 * from the remote VM, its reference is stored here. When the same DTO is passed
	 * back as an argument to a subsequent call, the stored reference is used
	 * directly — no need to mirror/write the DTO back.
	 */
	private final Map<DTO, ObjectReference> dtoReferenceMap = new IdentityHashMap<>();

	public JdiFrameworkBridge(VirtualMachine vm) {
		this.vm = vm;
	}

	VirtualMachine vm() {
		return vm;
	}

	/**
	 * Resolves a bundle by its numeric ID and returns a proxy for it. The returned
	 * proxy can be passed to service method calls that require a {@code Bundle}
	 * parameter.
	 *
	 * @param bundleId the bundle ID
	 * @return a proxy implementing the Bundle interface
	 */
	public Bundle resolveBundleProxy(long bundleId) {
		vm.suspend();
		try {
			ThreadReference thread = acquireEventSuspendedThread();
			ObjectReference bundleRef = resolveBundle(bundleId, thread);
			return createProxy(Bundle.class, bundleRef);
		} finally {
			vm.resume();
		}
	}

	/**
	 * Resolves the system bundle's {@code BundleContext} and returns a proxy for
	 * it. The proxy can be used to call any {@code BundleContext} method
	 * transparently via JDI.
	 *
	 * @return a proxy implementing the BundleContext interface
	 */
	public BundleContext resolveBundleContextProxy() {
		vm.suspend();
		try {
			ThreadReference thread = acquireEventSuspendedThread();
			ObjectReference framework = findFrameworkInstance();
			if (framework == null) {
				throw new IllegalStateException("Framework not found in remote VM");
			}
			ObjectReference bc = (ObjectReference) invokeMethodWithArgs(framework,
					JdiOsgiConstants.Bundle.METHOD_GET_BUNDLE_CONTEXT, Collections.emptyList(), thread);
			if (bc == null) {
				throw new IllegalStateException("BundleContext not available");
			}
			return createProxy(BundleContext.class, bc);
		} finally {
			vm.resume();
		}
	}

	/**
	 * Resolves a bundle by its symbolic name and returns a proxy for it. The
	 * returned proxy can be passed to service method calls that require a
	 * {@code Bundle} parameter.
	 *
	 * @param symbolicName the bundle symbolic name
	 * @return a proxy implementing the Bundle interface
	 */
	public Bundle resolveBundleProxy(String symbolicName) {
		vm.suspend();
		try {
			ThreadReference thread = acquireEventSuspendedThread();
			ObjectReference bundleRef = resolveBundle(symbolicName, thread);
			return createProxy(Bundle.class, bundleRef);
		} finally {
			vm.resume();
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T createProxy(Class<T> iface, ObjectReference remoteRef) {
		return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] { iface },
				new JdiProxyInvocationHandler(this, remoteRef));
	}

	/**
	 * Reads a DTO from the remote VM and registers its ObjectReference for later
	 * reuse when the DTO is passed back as an argument.
	 */
	<T extends DTO> T readAndRegisterDto(Class<T> dtoClass, ObjectReference ref, ThreadReference thread) {
		T dto = JdiDtoReader.readDto(dtoClass, ref, thread);
		dtoReferenceMap.put(dto, ref);
		return dto;
	}

	/**
	 * Looks up the remote ObjectReference for a DTO that was previously returned by
	 * this probe.
	 *
	 * @throws IllegalArgumentException if the DTO was not previously returned
	 */
	ObjectReference lookupDtoReference(DTO dto) {
		ObjectReference ref = dtoReferenceMap.get(dto);
		if (ref == null) {
			throw new IllegalArgumentException(
					"DTO " + dto.getClass().getSimpleName() + " was not returned by this probe — "
							+ "only DTOs obtained from previous calls can be passed as arguments");
		}
		return ref;
	}

	/**
	 * Waits for the OSGi framework to become ACTIVE by polling its state. The VM
	 * must be started with {@code suspend=y}. This method resumes the VM, then
	 * periodically suspends to check the framework state via
	 * {@code Bundle.getState()}.
	 *
	 * @return the ObjectReference to the Framework instance (VM is suspended on
	 *         return)
	 */
	public ObjectReference waitForFrameworkActive(long timeoutMs, long pollIntervalMs) {
		LOG.info("Waiting for OSGi framework to become ACTIVE (timeout: {}ms, poll: {}ms)", timeoutMs, pollIntervalMs);

		// Resume the VM — it was started with suspend=y
		vm.resume();

		long deadline = System.currentTimeMillis() + timeoutMs;

		while (System.currentTimeMillis() < deadline) {
			try {
				Thread.sleep(pollIntervalMs);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Interrupted while waiting for framework", e);
			}

			vm.suspend();

			try {
				ObjectReference framework = findFrameworkInstance();
				if (framework == null) {
					LOG.debug("Framework instance not yet available");
					vm.resume();
					continue;
				}

				ThreadReference thread = acquireEventSuspendedThread();
				int state = invokeGetState(framework, thread);
				LOG.debug("Framework state: {}", state);

				if (state == JdiOsgiConstants.Framework.ACTIVE) {
					LOG.info("OSGi framework is ACTIVE");
					// Allow installed bundles time to start before extracting DTOs
					vm.resume();
					Thread.sleep(pollIntervalMs);
					vm.suspend();
					return framework;
				}
			} catch (VMDisconnectedException e) {
				throw new IllegalStateException("VM disconnected while waiting for framework", e);
			} catch (Exception e) {
				LOG.debug("Error polling framework state: {}", e.getMessage());
			}

			vm.resume();
		}

		throw new IllegalStateException("OSGi framework did not become ACTIVE within " + timeoutMs + "ms");
	}

	/**
	 * Finds the Framework instance in the remote VM via interface implementors.
	 */
	ObjectReference findFrameworkInstance() {
		List<ReferenceType> interfaceTypes = vm.classesByName(JdiOsgiConstants.Framework.CLASS_NAME);
		if (!interfaceTypes.isEmpty() && interfaceTypes.get(0) instanceof InterfaceType iface) {
			for (ClassType impl : iface.implementors()) {
				List<ObjectReference> instances = impl.instances(1);
				if (!instances.isEmpty()) {
					LOG.debug("Found framework instance via interface implementor: {}", impl.name());
					return instances.get(0);
				}
			}
		}
		return null;
	}

	/**
	 * Invokes {@code Bundle.getState()} on the framework instance via JDI method
	 * invocation. Requires an event-suspended thread.
	 *
	 * @param framework the Framework instance
	 * @param thread    an event-suspended thread
	 * @return the framework state (e.g., Bundle.ACTIVE = 32)
	 */
	private int invokeGetState(ObjectReference framework, ThreadReference thread) {
		try {
			Method getState = JdiDtoReader.findMethod(framework.referenceType(),
					JdiOsgiConstants.Bundle.METHOD_GET_STATE, 0);
			Value result = framework.invokeMethod(thread, getState, Collections.emptyList(),
					ObjectReference.INVOKE_SINGLE_THREADED);
			if (result instanceof com.sun.jdi.IntegerValue iv) {
				return iv.value();
			}
			throw new IllegalStateException("getState() returned non-int: " + result);
		} catch (Exception e) {
			throw new RuntimeException("Failed to invoke getState() on framework", e);
		}
	}

	/**
	 * Extracts a FrameworkDTO using the OSGi adapt() API via JDI invokeMethod. The
	 * VM must be suspended on entry. It will be suspended on exit (success or
	 * failure).
	 *
	 * @param framework the ObjectReference to the Framework instance
	 * @return a FrameworkDTO with bundles, services, and properties
	 */
	public FrameworkDTO extractFrameworkDTO(ObjectReference framework) {
		try {
			ThreadReference thread = acquireEventSuspendedThread();
			LOG.debug("Acquired event-suspended thread: {}", thread.name());

			ClassObjectReference frameworkDtoClass = getClassMirror(JdiOsgiConstants.FrameworkDTO.CLASS_NAME, framework,
					thread);
			ObjectReference frameworkDtoRef = invokeAdapt(framework, frameworkDtoClass, thread);

			FrameworkDTO frameworkDTO = readAndRegisterDto(FrameworkDTO.class, frameworkDtoRef, thread);

			LOG.info("Extracted FrameworkDTO via OSGi API: {} bundles, {} services, {} properties",
					frameworkDTO.bundles.size(), frameworkDTO.services != null ? frameworkDTO.services.size() : 0,
					frameworkDTO.properties != null ? frameworkDTO.properties.size() : 0);

			return frameworkDTO;
		} catch (Exception e) {
			try {
				vm.suspend();
			} catch (Exception ignored) {
			}
			throw new RuntimeException("Failed to extract FrameworkDTO via OSGi API", e);
		}
	}

	/**
	 * Extracts a FrameworkStartLevelDTO using the OSGi adapt() API via JDI
	 * invokeMethod. The VM must be suspended on entry.
	 *
	 * @param framework the ObjectReference to the Framework instance
	 * @return a FrameworkStartLevelDTO
	 */
	public FrameworkStartLevelDTO extractFrameworkStartLevelDTO(ObjectReference framework) {
		try {
			ThreadReference thread = acquireEventSuspendedThread();
			LOG.debug("Acquired event-suspended thread: {}", thread.name());

			ClassObjectReference startLevelDtoClass = getClassMirror(JdiOsgiConstants.FrameworkStartLevelDTO.CLASS_NAME,
					framework, thread);
			ObjectReference startLevelDtoRef = invokeAdapt(framework, startLevelDtoClass, thread);

			return readAndRegisterDto(FrameworkStartLevelDTO.class, startLevelDtoRef, thread);
		} catch (Exception e) {
			LOG.warn("Failed to extract FrameworkStartLevelDTO via adapt(): {}", e.getMessage());
			FrameworkStartLevelDTO fallback = new FrameworkStartLevelDTO();
			fallback.startLevel = 1;
			fallback.initialBundleStartLevel = 1;
			return fallback;
		}
	}

	/**
	 * Takes a FrameworkDTO from a running VM (suspend, extract, resume).
	 */
	public FrameworkDTO takeFrameworkDTOFromRunningVm() {
		vm.suspend();
		try {
			ObjectReference framework = findFrameworkInstance();
			if (framework == null) {
				throw new IllegalStateException("Framework instance not found in running VM");
			}
			return extractFrameworkDTO(framework);
		} finally {
			vm.resume();
		}
	}

	/**
	 * Takes a FrameworkStartLevelDTO from a running VM (suspend, extract, resume).
	 */
	public FrameworkStartLevelDTO takeFrameworkStartLevelDTOFromRunningVm() {
		vm.suspend();
		try {
			ObjectReference framework = findFrameworkInstance();
			if (framework == null) {
				throw new IllegalStateException("Framework instance not found in running VM");
			}
			return extractFrameworkStartLevelDTO(framework);
		} finally {
			vm.resume();
		}
	}

	/**
	 * Invokes a no-argument method on a remote object via JDI. Also handles varargs
	 * methods (e.g., {@code getComponentDescriptionDTOs(Bundle...)}) by passing an
	 * empty array.
	 * <p>
	 * For varargs methods, prefers the version declared on the concrete class over
	 * the interface version — the concrete class's classloader has the array
	 * parameter type loaded, avoiding {@link ClassNotLoadedException} during JDI's
	 * internal varargs validation.
	 */
	Value invokeNoArgMethod(ObjectReference target, String methodName, ThreadReference thread) {
		try {
			ReferenceType type = target.referenceType();
			Method zeroParam = null;
			Method varArgsMethod = null;

			for (Method m : type.allMethods()) {
				if (!m.name().equals(methodName)) {
					continue;
				}
				if (m.argumentTypeNames().isEmpty()) {
					zeroParam = m;
					break;
				}
				// Varargs methods compile to a single array parameter.
				// Prefer the version declared on the concrete type — its
				// classloader has the array type loaded.
				if (m.isVarArgs() && m.argumentTypeNames().size() == 1) {
					if (varArgsMethod == null || m.declaringType().name().equals(type.name())) {
						varArgsMethod = m;
					}
				}
			}

			if (zeroParam != null) {
				return target.invokeMethod(thread, zeroParam, Collections.emptyList(),
						ObjectReference.INVOKE_SINGLE_THREADED);
			}

			if (varArgsMethod != null) {
				LOG.debug("Using varargs method '{}' declared on {}", methodName, varArgsMethod.declaringType().name());
				ArrayType arrayType = resolveVarArgsArrayType(varArgsMethod);
				ArrayReference emptyArray = arrayType.newInstance(0);
				return target.invokeMethod(thread, varArgsMethod, Collections.singletonList(emptyArray),
						ObjectReference.INVOKE_SINGLE_THREADED);
			}

			throw new IllegalArgumentException(
					"No method '" + methodName + "' with 0 parameters (or varargs) on " + type.name());
		} catch (Exception e) {
			throw new RuntimeException("Failed to invoke " + methodName + "() on " + target.referenceType().name(), e);
		}
	}

	/**
	 * Resolves the array type for a varargs method parameter. First tries direct
	 * type resolution; if that fails with {@link ClassNotLoadedException}, falls
	 * back to a VM-wide class lookup by name.
	 */
	private ArrayType resolveVarArgsArrayType(Method method) {
		try {
			return (ArrayType) method.argumentTypes().get(0);
		} catch (ClassNotLoadedException e) {
			String typeName = method.argumentTypeNames().get(0);
			List<ReferenceType> types = vm.classesByName(typeName);
			if (!types.isEmpty() && types.get(0) instanceof ArrayType at) {
				return at;
			}
			throw new RuntimeException(
					"Cannot resolve array type '" + typeName + "' for varargs method " + method.name(), e);
		}
	}

	/**
	 * Converts Java objects to JDI Values for method invocation. Accepts only
	 * strings, primitives, and DTOs that were previously returned by this probe
	 * (looked up via the internal reference map).
	 */
	List<Value> convertArgs(Object[] args) {
		List<Value> jdiArgs = new ArrayList<>();
		for (Object arg : args) {
			if (arg instanceof String s) {
				jdiArgs.add(vm.mirrorOf(s));
			} else if (arg instanceof Long l) {
				jdiArgs.add(vm.mirrorOf(l));
			} else if (arg instanceof Integer i) {
				jdiArgs.add(vm.mirrorOf(i));
			} else if (arg instanceof Boolean b) {
				jdiArgs.add(vm.mirrorOf(b));
			} else if (arg instanceof Short s) {
				jdiArgs.add(vm.mirrorOf(s));
			} else if (arg instanceof Byte b) {
				jdiArgs.add(vm.mirrorOf(b));
			} else if (arg instanceof Character c) {
				jdiArgs.add(vm.mirrorOf(c));
			} else if (arg instanceof Float f) {
				jdiArgs.add(vm.mirrorOf(f));
			} else if (arg instanceof Double d) {
				jdiArgs.add(vm.mirrorOf(d));
			} else if (arg instanceof DTO dto) {
				jdiArgs.add(lookupDtoReference(dto));
			} else if (arg == null) {
				jdiArgs.add(null);
			} else {
				throw new IllegalArgumentException("Unsupported arg type: " + arg.getClass().getName()
						+ ". Only strings, primitives, and previously-returned DTOs are accepted.");
			}
		}
		return jdiArgs;
	}

	/**
	 * Resolves a Bundle in the remote VM by its numeric ID.
	 */
	ObjectReference resolveBundle(long bundleId, ThreadReference thread) {
		ObjectReference framework = findFrameworkInstance();
		if (framework == null) {
			throw new IllegalStateException("Framework not found in remote VM");
		}
		ObjectReference bc = (ObjectReference) invokeMethodWithArgs(framework,
				JdiOsgiConstants.Bundle.METHOD_GET_BUNDLE_CONTEXT, Collections.emptyList(), thread);
		if (bc == null) {
			throw new IllegalStateException("BundleContext not available");
		}
		Method getBundle = JdiDtoReader.findMethod(bc.referenceType(), JdiOsgiConstants.BundleContext.METHOD_GET_BUNDLE,
				List.of("long"));
		try {
			Value result = bc.invokeMethod(thread, getBundle, List.of(vm.mirrorOf(bundleId)),
					ObjectReference.INVOKE_SINGLE_THREADED);
			if (result == null || !(result instanceof ObjectReference bundleRef)) {
				throw new IllegalArgumentException("No bundle with id " + bundleId);
			}
			return bundleRef;
		} catch (Exception e) {
			throw new RuntimeException("Failed to resolve bundle with id " + bundleId, e);
		}
	}

	/**
	 * Resolves a Bundle in the remote VM by its symbolic name (first match).
	 */
	ObjectReference resolveBundle(String symbolicName, ThreadReference thread) {
		ObjectReference framework = findFrameworkInstance();
		if (framework == null) {
			throw new IllegalStateException("Framework not found in remote VM");
		}
		ObjectReference bc = (ObjectReference) invokeMethodWithArgs(framework,
				JdiOsgiConstants.Bundle.METHOD_GET_BUNDLE_CONTEXT, Collections.emptyList(), thread);
		if (bc == null) {
			throw new IllegalStateException("BundleContext not available");
		}
		try {
			Method getBundles = JdiDtoReader.findMethod(bc.referenceType(),
					JdiOsgiConstants.BundleContext.METHOD_GET_BUNDLES, 0);
			Value result = bc.invokeMethod(thread, getBundles, Collections.emptyList(),
					ObjectReference.INVOKE_SINGLE_THREADED);
			if (!(result instanceof ArrayReference arr)) {
				throw new IllegalStateException("getBundles() returned unexpected: " + result);
			}
			for (Value v : arr.getValues()) {
				if (!(v instanceof ObjectReference bundleRef)) {
					continue;
				}
				Method getName = JdiDtoReader.findMethod(bundleRef.referenceType(),
						JdiOsgiConstants.Bundle.METHOD_GET_SYMBOLIC_NAME, 0);
				Value nameVal = bundleRef.invokeMethod(thread, getName, Collections.emptyList(),
						ObjectReference.INVOKE_SINGLE_THREADED);
				if (nameVal instanceof StringReference sr && symbolicName.equals(sr.value())) {
					return bundleRef;
				}
			}
			throw new IllegalArgumentException("No bundle with symbolic name '" + symbolicName + "'");
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Failed to resolve bundle '" + symbolicName + "'", e);
		}
	}

	/**
	 * Holds the result of a filtered service lookup: the service object, the
	 * BundleContext, and the ServiceReference. The caller must call
	 * {@link #ungetService(ServiceLookup, ThreadReference)} when done.
	 */
	record ServiceLookup(ObjectReference service, ObjectReference bundleContext, ObjectReference serviceReference) {
	}

	/**
	 * Finds a service instance using an LDAP filter on service properties. Uses
	 * {@code BundleContext.getServiceReferences(String, String)} and
	 * {@code BundleContext.getService(ServiceReference)}.
	 * <p>
	 * The caller must call {@link #ungetService(ServiceLookup, ThreadReference)}
	 * when done with the returned service.
	 */
	ServiceLookup findServiceInstance(String interfaceName, String filter, ThreadReference thread) {
		List<ServiceLookup> all = findAllServiceInstances(interfaceName, filter, thread);
		if (all.isEmpty()) {
			throw new IllegalStateException("Service not found: " + interfaceName);
		}
		return all.get(0);
	}

	/**
	 * Releases a service obtained via
	 * {@link #findServiceInstance(String, String, ThreadReference)}. Calls
	 * {@code BundleContext.ungetService(ServiceReference)}.
	 */
	void ungetService(ServiceLookup lookup, ThreadReference thread) {
		try {
			Method ungetService = JdiDtoReader.findMethod(lookup.bundleContext().referenceType(),
					JdiOsgiConstants.BundleContext.METHOD_UNGET_SERVICE, 1);
			lookup.bundleContext().invokeMethod(thread, ungetService, List.of(lookup.serviceReference()),
					ObjectReference.INVOKE_SINGLE_THREADED);
		} catch (Exception e) {
			LOG.warn("Failed to unget service: {}", e.getMessage());
		}
	}

	/**
	 * Finds all service instances matching the given interface name and optional
	 * filter. Returns a list of {@link ServiceLookup} records, each containing the
	 * service object, BundleContext, and ServiceReference.
	 * <p>
	 * The caller must call {@link #ungetService(ServiceLookup, ThreadReference)}
	 * for each returned lookup when done.
	 */
	List<ServiceLookup> findAllServiceInstances(String interfaceName, String filter, ThreadReference thread) {
		ObjectReference framework = findFrameworkInstance();
		if (framework == null) {
			throw new IllegalStateException("Framework not found in remote VM");
		}
		ObjectReference bc = (ObjectReference) invokeMethodWithArgs(framework,
				JdiOsgiConstants.Bundle.METHOD_GET_BUNDLE_CONTEXT, Collections.emptyList(), thread);
		if (bc == null) {
			throw new IllegalStateException("BundleContext not available");
		}
		try {
			Method getServiceRefs = JdiDtoReader.findMethod(bc.referenceType(),
					JdiOsgiConstants.BundleContext.METHOD_GET_SERVICE_REFERENCES,
					List.of("java.lang.String", "java.lang.String"));
			List<Value> args = new ArrayList<>();
			args.add(vm.mirrorOf(interfaceName));
			args.add(filter != null ? vm.mirrorOf(filter) : null);
			Value refs = bc.invokeMethod(thread, getServiceRefs, args, ObjectReference.INVOKE_SINGLE_THREADED);

			List<ServiceLookup> results = new ArrayList<>();
			if (refs instanceof ArrayReference arr) {
				Method getService = JdiDtoReader.findMethod(bc.referenceType(),
						JdiOsgiConstants.BundleContext.METHOD_GET_SERVICE, 1);
				for (int i = 0; i < arr.length(); i++) {
					ObjectReference serviceRef = (ObjectReference) arr.getValue(i);
					Value service = bc.invokeMethod(thread, getService, List.of(serviceRef),
							ObjectReference.INVOKE_SINGLE_THREADED);
					if (service instanceof ObjectReference serviceObj) {
						results.add(new ServiceLookup(serviceObj, bc, serviceRef));
					}
				}
			}
			return results;
		} catch (Exception e) {
			throw new RuntimeException("Failed to find services '" + interfaceName + "' with filter '" + filter + "'",
					e);
		}
	}

	/**
	 * Invokes a method on a remote object with given arguments.
	 */
	Value invokeMethodWithArgs(ObjectReference target, String methodName, List<? extends Value> args,
			ThreadReference thread) {
		try {
			Method method = JdiDtoReader.findMethod(target.referenceType(), methodName, args.size());
			return target.invokeMethod(thread, method, args, ObjectReference.INVOKE_SINGLE_THREADED);
		} catch (Exception e) {
			throw new RuntimeException("Failed to invoke " + methodName + " on " + target.referenceType().name(), e);
		}
	}

	/**
	 * Acquires an event-suspended thread suitable for invokeMethod().
	 * <p>
	 * The VM must be suspended on entry (e.g., from waitForFrameworkActive). On
	 * success, the VM is suspended again (SUSPEND_ALL from step event) and the
	 * returned thread is event-suspended.
	 * <p>
	 * Strategy: find a waiting thread (in Object.wait/LockSupport.park), set up a
	 * StepRequest, interrupt the thread to wake it from native wait, resume VM, and
	 * catch the step event when it returns to Java code.
	 */
	ThreadReference acquireEventSuspendedThread() {
		// Pass 1: look for a thread with a non-native top frame (ideal)
		List<ThreadReference> javaTopThreads = new ArrayList<>();
		for (ThreadReference t : vm.allThreads()) {
			try {
				if (t.frameCount() > 0 && !t.name().startsWith(THREAD_PREFIX_JDI)
						&& !t.frame(0).location().method().isNative()) {
					javaTopThreads.add(t);
				}
			} catch (Exception e) {
				// skip
			}
		}

		for (ThreadReference t : javaTopThreads) {
			try {
				LOG.debug("Trying thread '{}' with Java top frame for step", t.name());
				return stepThread(t, StepRequest.STEP_OVER);
			} catch (Exception e) {
				LOG.debug("Step on thread '{}' failed: {}", t.name(), e.getMessage());
				// stepThread guarantees VM is suspended on failure — do NOT suspend again
				// to avoid incrementing the suspend count (which would block subsequent
				// resumes)
			}
		}

		// Pass 2: find a thread blocked in native code (Object.wait, park, etc.)
		// and interrupt it to force it back into Java code
		LOG.debug("No usable thread via step, trying interrupt strategy");
		return acquireViaInterrupt();
	}

	/**
	 * Steps a thread and returns it as event-suspended. Drains stale events (e.g.,
	 * VMStartEvent) before the step event.
	 */
	private ThreadReference stepThread(ThreadReference thread, int stepDepth) {
		EventRequestManager erm = vm.eventRequestManager();
		StepRequest stepRequest = erm.createStepRequest(thread, StepRequest.STEP_MIN, stepDepth);
		stepRequest.addCountFilter(1);
		stepRequest.setSuspendPolicy(StepRequest.SUSPEND_ALL);
		stepRequest.enable();

		try {
			vm.resume();

			long deadline = System.currentTimeMillis() + 10000;
			while (System.currentTimeMillis() < deadline) {
				long remaining = deadline - System.currentTimeMillis();
				if (remaining <= 0)
					break;

				EventSet eventSet = vm.eventQueue().remove(remaining);
				if (eventSet == null)
					break;

				EventIterator iter = eventSet.eventIterator();
				while (iter.hasNext()) {
					Event event = iter.next();
					if (event instanceof StepEvent stepEvent) {
						return stepEvent.thread();
					}
					if (event instanceof com.sun.jdi.event.VMDeathEvent
							|| event instanceof com.sun.jdi.event.VMDisconnectEvent) {
						throw new IllegalStateException("VM died/disconnected during step");
					}
				}
				// Not our event (e.g., stale VMStartEvent) — resume and keep waiting
				eventSet.resume();
			}

			vm.suspend();
			throw new IllegalStateException("Timed out waiting for step event");
		} catch (InterruptedException e) {
			vm.suspend();
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting for step event", e);
		} finally {
			erm.deleteEventRequest(stepRequest);
		}
	}

	/**
	 * Acquires an event-suspended thread by interrupting a safe daemon thread.
	 * <p>
	 * When all threads are blocked in native waits (Object.wait, LockSupport.park),
	 * we interrupt a JVM-internal daemon thread to force it back into Java code,
	 * then catch the step event. We avoid the main thread (which would cause
	 * shutdown).
	 */
	private ThreadReference acquireViaInterrupt() {
		// Find a daemon thread in Object.wait() — these respond to Thread.interrupt().
		// Threads in other native methods (waitForReferencePendingList, park) may NOT
		// be interruptible. We specifically look for threads whose top frame is "wait0"
		// which is the native implementation of Object.wait().
		ThreadReference waitThread = null; // thread in wait0 (interruptible)
		ThreadReference parkThread = null; // thread in park (might work with unpark)

		for (ThreadReference t : vm.allThreads()) {
			try {
				if (t.frameCount() == 0 || t.name().startsWith(THREAD_PREFIX_JDI)
						|| t.name().equals(THREAD_NAME_MAIN)) {
					continue;
				}
				String topMethod = t.frame(0).location().method().name();
				if (NATIVE_METHOD_WAIT.equals(topMethod) && waitThread == null) {
					waitThread = t;
				} else if (NATIVE_METHOD_PARK.equals(topMethod) && parkThread == null) {
					parkThread = t;
				}
			} catch (Exception e) {
				// skip
			}
		}

		ThreadReference targetThread = waitThread != null ? waitThread : parkThread;
		if (targetThread == null) {
			throw new IllegalStateException("No suitable daemon thread found to interrupt for event-suspended thread");
		}

		LOG.debug("Interrupting thread '{}' (status={}) to acquire event-suspended thread", targetThread.name(),
				targetThread.status());

		EventRequestManager erm = vm.eventRequestManager();
		StepRequest stepRequest = erm.createStepRequest(targetThread, StepRequest.STEP_MIN, StepRequest.STEP_OUT);
		stepRequest.addCountFilter(1);
		stepRequest.setSuspendPolicy(StepRequest.SUSPEND_ALL);
		stepRequest.enable();

		try {
			// Interrupt while still suspended — takes effect on resume
			targetThread.interrupt();
			vm.resume();

			// Wait for step event — may need to drain non-step events first
			long deadline = System.currentTimeMillis() + 10000;
			while (System.currentTimeMillis() < deadline) {
				long remaining = deadline - System.currentTimeMillis();
				if (remaining <= 0)
					break;

				EventSet eventSet = vm.eventQueue().remove(remaining);
				if (eventSet == null) {
					break;
				}

				EventIterator iter = eventSet.eventIterator();
				while (iter.hasNext()) {
					Event event = iter.next();
					LOG.debug("Received event: {}", event.getClass().getSimpleName());
					if (event instanceof StepEvent stepEvent) {
						erm.deleteEventRequest(stepRequest);
						return stepEvent.thread();
					}
					if (event instanceof com.sun.jdi.event.VMDeathEvent
							|| event instanceof com.sun.jdi.event.VMDisconnectEvent) {
						throw new IllegalStateException("VM died/disconnected after thread interrupt");
					}
				}
				// Not our event — resume and keep waiting
				eventSet.resume();
			}

			vm.suspend();
			erm.deleteEventRequest(stepRequest);
			throw new IllegalStateException(
					"Timed out waiting for step event after interrupting thread '" + targetThread.name() + "'");
		} catch (InterruptedException e) {
			vm.suspend();
			erm.deleteEventRequest(stepRequest);
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting for step event", e);
		}
	}

	/**
	 * Gets the Class mirror for a class name in the remote VM. Falls back to
	 * Bundle.loadClass() on the framework (system bundle) if the class isn't loaded
	 * yet — this uses the OSGi classloader which has access to framework packages.
	 */
	private ClassObjectReference getClassMirror(String className, ObjectReference framework, ThreadReference thread) {
		List<ReferenceType> types = vm.classesByName(className);
		if (!types.isEmpty()) {
			ReferenceType type = types.get(0);
			if (type instanceof ClassType ct) {
				return ct.classObject();
			}
			if (type instanceof InterfaceType it) {
				return it.classObject();
			}
		}

		// Class not loaded yet — load via framework.loadClass() (OSGi classloader)
		LOG.debug("Class '{}' not loaded, using framework.loadClass()", className);
		return loadClassViaFramework(className, framework, thread);
	}

	/**
	 * Loads a class via Bundle.loadClass(String) on the framework instance. The
	 * framework is the system bundle (bundle 0) and has access to all
	 * org.osgi.framework.* packages.
	 */
	private ClassObjectReference loadClassViaFramework(String className, ObjectReference framework,
			ThreadReference thread) {
		try {
			Method loadClass = JdiDtoReader.findMethod(framework.referenceType(),
					JdiOsgiConstants.Bundle.METHOD_LOAD_CLASS, 1);

			StringReference classNameRef = vm.mirrorOf(className);
			Value result = framework.invokeMethod(thread, loadClass, Collections.singletonList(classNameRef),
					ObjectReference.INVOKE_SINGLE_THREADED);

			if (result instanceof ClassObjectReference cor) {
				return cor;
			}
			throw new IllegalStateException("framework.loadClass('" + className + "') returned unexpected: " + result);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load class '" + className + "' via framework.loadClass()", e);
		}
	}

	/**
	 * Invokes bundle.adapt(clazz) via JDI.
	 */
	private ObjectReference invokeAdapt(ObjectReference target, ClassObjectReference clazz, ThreadReference thread) {
		try {
			// Find adapt(Class) method — it's on Bundle interface (or Framework which
			// extends Bundle)
			Method adaptMethod = JdiDtoReader.findMethod(target.referenceType(), JdiOsgiConstants.Bundle.METHOD_ADAPT,
					1);

			Value result = target.invokeMethod(thread, adaptMethod, Collections.singletonList(clazz),
					ObjectReference.INVOKE_SINGLE_THREADED);

			if (result == null) {
				throw new IllegalStateException("adapt() returned null for " + clazz.reflectedType().name());
			}

			if (result instanceof ObjectReference or) {
				return or;
			}

			throw new IllegalStateException("adapt() returned unexpected type: " + result);
		} catch (Exception e) {
			throw new RuntimeException("Failed to invoke adapt() on " + target.referenceType().name(), e);
		}
	}

}

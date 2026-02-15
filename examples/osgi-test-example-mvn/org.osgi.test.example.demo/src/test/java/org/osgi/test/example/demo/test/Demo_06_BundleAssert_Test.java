package org.osgi.test.example.demo.test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.test.assertj.bundle.AbstractBundleAssert;
import org.osgi.test.assertj.bundle.BundleAssert;
import org.osgi.test.assertj.bundleevent.BundleEventAssert;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectBundleInstaller;
import org.osgi.test.common.install.BundleInstaller;

public class Demo_06_BundleAssert_Test {

	@InjectBundleContext
	BundleContext bc;

	@Test
	void bundleInstaller_EmbeddedPath(@InjectBundleInstaller BundleInstaller bundleInstaller) throws Exception {

		CountDownLatch latch = new CountDownLatch(1);
		List<BundleEvent> bundleEvents = new ArrayList<>();

		bc.addBundleListener(new BundleListener() {
			@Override
			public void bundleChanged(BundleEvent event) {
				bundleEvents.add(event);
				latch.countDown();
			}
		});

		Bundle bu = bundleInstaller.installBundle("embeddedTestBundles/tb1.jar", false);
		
		latch.await(5, TimeUnit.SECONDS);

		//!
		BundleAssert.assertThat(bu)//
				.isNotFragment().isInState(Bundle.INSTALLED);

		BundleEvent bundleEvent = bundleEvents.get(0);

		// !
		BundleEventAssert beAssert = BundleEventAssert.assertThat(bundleEvent)//
				.isOfTypeMaskedBy(BundleEvent.INSTALLED);

		AbstractBundleAssert ba = beAssert.hasBundleThat();//
		ba.hasSymbolicName("org.osgi.test.example.demo.tb1")//
				.isNotInStateMask(Bundle.ACTIVE | Bundle.STARTING | Bundle.UNINSTALLED | Bundle.STOPPING).isNotFragment()
				.hasResource("org/osgi/test/example/demo/testbundle/tb1/TestBundle1BundleActivator.class")//
				.hasLastModifiedDateThat()//
				.isAfter(Instant.now().minusSeconds(2));

		ba.hasVersionThat()//
				.hasMajor(1)//
				.hasMinor(4)//
				.hasMicro(0)//
				.hasQualifier("SNAPSHOT");
	}

}

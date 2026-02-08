package org.osgi.test.example.demo.test;

import java.net.URI;
import java.net.URL;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.test.assertj.bundle.BundleAssert;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectBundleInstaller;
import org.osgi.test.common.install.BundleInstaller;
import org.osgi.test.common.install.BundleInstaller.EmbeddedLocation;

public class Demo_05_BundleInstaller_Test {

	@Test
	void bundleInstaller_EmbeddedPath(@InjectBundleInstaller BundleInstaller bundleInstaller) throws Exception {
		// in the -test-jar exists tb1.jar
		bundleInstaller.installBundle("embeddedTestBundles/tb1.jar");
	}

	@Test
	void bundleInstaller_EmbeddedPathNoStart(@InjectBundleInstaller BundleInstaller bundleInstaller) throws Exception {

		Bundle installedBundle = bundleInstaller.installBundle("embeddedTestBundles/tb1.jar", false);
		installedBundle.start();
	}

	@Test
	void bundleInstaller_EmbeddedLocation_spec(@InjectBundleInstaller BundleInstaller bundleInstaller, @InjectBundleContext BundleContext bc) throws Exception {

		// "bundle.symbolic.name:1.2.3:/path/file.jar"
		EmbeddedLocation el = EmbeddedLocation.of(bc, "org.osgi.test.example.demo-tests:1.0.0.SNAPSHOT:/embeddedTestBundles/tb1.jar");
		bundleInstaller.installBundle(el, true);
	}

	@Test
	void bundleInstaller_EmbeddedLocation_fqn(@InjectBundleInstaller BundleInstaller bundleInstaller, @InjectBundleContext BundleContext bc) throws Exception {

		EmbeddedLocation el = EmbeddedLocation.of("org.osgi.test.example.demo-tests", new Version(1, 0, 0, "SNAPSHOT"), "embeddedTestBundles", "tb1.jar");
		bundleInstaller.installBundle(el, true);
	}

	@Test
	void bundleInstaller_EmbeddedLocation_Opt(@InjectBundleInstaller BundleInstaller bundleInstaller, @InjectBundleContext BundleContext bc) throws Exception {

		EmbeddedLocation el = EmbeddedLocation.of(bc, Optional.of("org.osgi.test.example.demo-tests"), Optional.empty(), Optional.of("embeddedTestBundles"), "tb1.jar");
		bundleInstaller.installBundle(el, true);
	}

	@Test
	void bundleInstallerUrl(@InjectBundleInstaller BundleInstaller bundleInstaller) throws Exception {

		
		URL url = URI.create("https://repo1.maven.org/maven2/biz/aQute/bnd/biz.aQute.bnd.annotation/7.0.0/biz.aQute.bnd.annotation-7.0.0.jar").toURL();
		Bundle installedBundle = bundleInstaller.installBundle(url, true);
		installedBundle.start();
	}
}

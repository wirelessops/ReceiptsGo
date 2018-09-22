package co.smartreceipts.android.utils;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class ConfigurableStaticFeatureTest {

    @Test
    public void assertValues() {
        // Since we sometimes toggle these for debug purposes, I've added these tests to prevent us
        // from accidentally breaking things (e.g. by uploading the usage of a beta endpoint to master)
        assertTrue(ConfigurableStaticFeature.UseProductionEndpoint.isEnabled(RuntimeEnvironment.application));
        assertTrue(ConfigurableStaticFeature.CompatPdfRendering.isEnabled(RuntimeEnvironment.application));
        assertTrue(ConfigurableStaticFeature.AutomaticallyLaunchLastTrip.isEnabled(RuntimeEnvironment.application));
    }
}
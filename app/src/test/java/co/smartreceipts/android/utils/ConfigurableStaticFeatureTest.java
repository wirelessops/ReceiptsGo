package co.smartreceipts.android.utils;

import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ConfigurableStaticFeatureTest {

    @Test
    public void assertValues() {
        // Since we sometimes toggle these for debug purposes, I've added these tests to prevent us
        // from accidentally breaking things (e.g. by uploading the usage of a beta endpoint to master)
        assertTrue(ConfigurableStaticFeature.UseProductionEndpoint.isEnabled(ApplicationProvider.getApplicationContext()));
        assertTrue(ConfigurableStaticFeature.CompatPdfRendering.isEnabled(ApplicationProvider.getApplicationContext()));
        assertTrue(ConfigurableStaticFeature.AutomaticallyLaunchLastTrip.isEnabled(ApplicationProvider.getApplicationContext()));
    }
}
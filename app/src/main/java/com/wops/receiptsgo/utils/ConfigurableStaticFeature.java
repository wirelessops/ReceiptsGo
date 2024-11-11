package com.wops.receiptsgo.utils;

import android.content.Context;
import androidx.annotation.NonNull;

/**
 * This differs from the {@link ConfigurableResourceFeature} in that it is only intended to be
 * managed via a static boolean value as opposed to a resource configuration. Features added here
 * should not be considered as candidates for white-labelling.
 */
public enum ConfigurableStaticFeature implements Feature {

    /**
     * Determines if we automatically open the UI screen for our last trip. Currently set to false
     * while we generally improve app start times and screen performance
     */
    AutomaticallyLaunchLastTrip(true),

    /**
     * Since Android has a bug on pre-O devices for PDF rendering, this enables "Compat" PDF rendering
     * in which we use our local '.so' libraries for PDF generation
     */
    CompatPdfRendering(true),

    /**
     * Indicates that we should use the production SmartReceipts.co endpoint (ie instead of beta).
     * This value should always be set to {@code true} in release builds
     */
    UseProductionEndpoint(true);

    private final boolean isEnabled;

    ConfigurableStaticFeature(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean isEnabled(@NonNull Context context) {
        return isEnabled;
    }

}

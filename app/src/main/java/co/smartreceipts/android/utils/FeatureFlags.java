package co.smartreceipts.android.utils;

public enum FeatureFlags implements Feature {

    /**
     * Tracks if the OCR feature is enabled or not
     */
    Ocr(true),

    /**
     * Allows us to manage syncing of organization settings across users
     */
    OrganizationSyncing(false),

    /**
     * Since Android has a bug on pre-O devices for PDF rendering, this enables "Compat" PDF rendering
     * in which we use our local '.so' libraries for PDF generation
     */
    CompatPdfRendering(true),

    /**
     * Enables the graphs tab for users to see their spending habits
     */
    Graphs(true),

    /**
     * Indicates that we should use the production SmartReceipts.co endpoint (ie instead of beta).
     * This value should always be set to {@code true} in release builds
     */
    UseProductionEndpoint(true);

    private final boolean isEnabled;

    FeatureFlags(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}

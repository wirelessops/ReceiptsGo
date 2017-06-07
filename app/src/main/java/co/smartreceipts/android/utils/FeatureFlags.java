package co.smartreceipts.android.utils;

public enum FeatureFlags implements Feature {

    Ocr(true), OrganizationSyncing(false), CompatPdfRendering(true), UseNativeAds(false), Graphs(true);

    private final boolean isEnabled;

    FeatureFlags(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}

package co.smartreceipts.android.utils;

import android.content.Context;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;

import co.smartreceipts.android.R;

public enum ConfigurableResourceFeature implements Feature {

    /**
     * Tracks if the settings menu is enabled or not
     */
    SettingsMenu(R.bool.config_is_the_settings_menu_enabled),

    /**
     * Tracks if the graphs tab is enabled or not
     */
    GraphsScreen(R.bool.config_is_the_graphs_screen_enabled),

    /**
     * Tracks if the distance tab is enabled or not
     */
    DistanceScreen(R.bool.config_is_the_distance_screen_enabled),

    /**
     * Tracks if we can use "Text-Only" receipts
     */
    TextOnlyReceipts(R.bool.config_is_the_text_only_receipt_feature_enabled),

    /**
     * Tracks if the OCR feature is enabled or not
     */
    Ocr(R.bool.config_is_ocr_enabled),

    /**
     * Allows us to manage syncing of organization settings across users
     */
    OrganizationSyncing(R.bool.config_is_organization_syncing_enabled);

    private final int booleanResID;

    ConfigurableResourceFeature(@BoolRes int booleanResID) {
        this.booleanResID = booleanResID;
    }

    @Override
    public boolean isEnabled(@NonNull Context context) {
        return context.getResources().getBoolean(booleanResID);
    }

}

package com.wops.receiptsgo.widget.tooltip.report.generate.data;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

import co.smartreceipts.core.di.scopes.ApplicationScope;

@ApplicationScope
public class GenerateInfoTooltipPreferencesStorage implements GenerateInfoTooltipStorage {

    @Inject
    Context appContext;

    private static final class Keys {
        /**
         * Key to get preferences related to the generate info tooltip
         */
        private static final String GENERATE_TOOLTIP_PREFERENCES = "Generate Tooltip Preferences";
        /**
         * Key to track if the tooltip was previously dismissed by user
         */
        private static final String GENERATE_TOOLTIP_WAS_DISMISSED = "Tooltip was dismissed";
        /**
         * Key to track did the user generate a report
         */
        private static final String USER_GENERATED_REPORT = "Report was generated";
    }

    @Inject
    public GenerateInfoTooltipPreferencesStorage() {
    }

    @Override
    public void tooltipWasDismissed() {
        getPreferencesEditor()
                .putBoolean(Keys.GENERATE_TOOLTIP_WAS_DISMISSED, true)
                .apply();

    }

    @Override
    public boolean wasTooltipDismissed() {
        return getSharedPreferences().getBoolean(Keys.GENERATE_TOOLTIP_WAS_DISMISSED, false);
    }

    @Override
    public void reportWasGenerated() {
        getPreferencesEditor()
                .putBoolean(Keys.USER_GENERATED_REPORT, true)
                .apply();
    }

    @Override
    public boolean wasReportEverGenerated() {
        return getSharedPreferences().getBoolean(Keys.USER_GENERATED_REPORT, false);
    }


    private SharedPreferences getSharedPreferences() {
        return appContext.getSharedPreferences(Keys.GENERATE_TOOLTIP_PREFERENCES, Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getPreferencesEditor() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.edit();
    }
}

package com.wops.receiptsgo.settings.widget;

import com.wops.receiptsgo.R;

public class PreferenceHeaderReportOutputFragment
        extends AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_output;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesOutput(this);
    }
}

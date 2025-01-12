package com.wops.receiptsgo.settings.widget;

import com.wops.receiptsgo.R;

public class PreferenceHeaderProFragment extends
        AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_pro;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePlusPreferences(this);
    }
}

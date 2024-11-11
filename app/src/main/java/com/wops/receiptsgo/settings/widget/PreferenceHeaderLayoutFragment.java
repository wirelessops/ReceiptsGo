package com.wops.receiptsgo.settings.widget;

import com.wops.receiptsgo.R;

public class PreferenceHeaderLayoutFragment extends
        AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_layout;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesLayoutCustomizations(this);
    }
}

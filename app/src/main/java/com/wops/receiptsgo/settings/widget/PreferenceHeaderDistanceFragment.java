package com.wops.receiptsgo.settings.widget;

import com.wops.receiptsgo.R;

public class PreferenceHeaderDistanceFragment extends
        AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_distance;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesDistance(this);
    }
}

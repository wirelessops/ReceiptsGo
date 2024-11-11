package com.wops.receiptsgo.settings.widget;

import com.wops.receiptsgo.R;

public class PreferenceHeaderGeneralFragment extends
        AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_general;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesGeneral(this);
    }
}

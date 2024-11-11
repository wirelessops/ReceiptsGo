package com.wops.receiptsgo.settings.widget;

import com.wops.receiptsgo.R;

public class PreferenceHeaderEmailFragment extends
        AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_email;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesEmail(this);
    }
}

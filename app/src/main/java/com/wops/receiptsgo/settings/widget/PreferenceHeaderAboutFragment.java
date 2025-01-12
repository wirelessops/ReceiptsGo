package com.wops.receiptsgo.settings.widget;

import com.wops.receiptsgo.R;

public class PreferenceHeaderAboutFragment extends
        AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_about;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesAbout(this);
    }
}

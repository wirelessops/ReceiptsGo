package com.wops.receiptsgo.settings.widget;

import com.wops.receiptsgo.R;

public class PreferenceHeaderHelpFragment extends
        AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_help;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesHelp(this);
    }
}

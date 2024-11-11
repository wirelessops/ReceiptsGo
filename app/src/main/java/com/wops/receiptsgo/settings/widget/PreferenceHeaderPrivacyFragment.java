package com.wops.receiptsgo.settings.widget;

import com.wops.receiptsgo.R;

public class PreferenceHeaderPrivacyFragment extends AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_privacy;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesPrivacy(this);
    }
}

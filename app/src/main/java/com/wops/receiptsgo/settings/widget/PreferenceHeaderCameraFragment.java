package com.wops.receiptsgo.settings.widget;

import com.wops.receiptsgo.R;

public class PreferenceHeaderCameraFragment extends
        AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_camera;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesCamera(this);
    }
}

package co.smartreceipts.android.settings.widget;

import co.smartreceipts.android.R;

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

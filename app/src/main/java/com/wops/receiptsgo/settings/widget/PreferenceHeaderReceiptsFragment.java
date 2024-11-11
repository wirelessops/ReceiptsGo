package com.wops.receiptsgo.settings.widget;

import com.wops.receiptsgo.R;

public class PreferenceHeaderReceiptsFragment extends AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_receipts;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesReceipts(this);
    }
}

package co.smartreceipts.android.settings.widget;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import androidx.annotation.XmlRes;
import android.view.MenuItem;

import co.smartreceipts.android.R;
import co.smartreceipts.android.settings.UserPreferenceManager;

public abstract class AbstractPreferenceHeaderFragment extends android.preference.PreferenceFragment implements UniversalPreferences {

    protected SettingsActivity mSettingsActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof SettingsActivity) {
            mSettingsActivity = (SettingsActivity) activity;
        } else {
            throw new RuntimeException("AbstractPreferenceHeaderFragment requires a SettingsActivity");
        }
    }

    @XmlRes
    public abstract int getPreferencesResourceId();

    public abstract void configurePreferences();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingsActivity.setFragmentHeaderIsShowing(true);
        setHasOptionsMenu(true); // Required to simulate up navigation
        getPreferenceManager().setSharedPreferencesName(UserPreferenceManager.PREFERENCES_FILE_NAME);

        addPreferencesFromResource(getPreferencesResourceId());
        configurePreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        final ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getArguments().getString(getString(R.string.pref_header_key)));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSettingsActivity.setFragmentHeaderIsShowing(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSettingsActivity = null; //Garbage Collection
    }

    @Override
    public Preference findPreference(int stringId) {
        return findPreference(getString(stringId));
    }

}

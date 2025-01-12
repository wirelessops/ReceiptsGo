package com.wops.receiptsgo.settings.widget;

import android.preference.Preference;

/**
 * I created this class so to create a unified interface when attempting
 * to find a particular preference from either the Activity or Fragment.
 * 
 * Without this, I would have needed to duplicate code (once in the activity,
 * once in the fragment). This hacky interface allows me to use the same set of 
 * code for both.
 * 
 * @author Will Baumann
 *
 */
public interface UniversalPreferences {

	Preference findPreference(CharSequence charSequence);
	Preference findPreference(int stringId);
}

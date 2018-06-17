package co.smartreceipts.android.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

/**
 * Tracks the various instances in which we store {@link SharedPreferences} in the app that are not
 * managed via the default {@link PreferenceManager#getDefaultSharedPreferences(Context)}. This mostly
 * exist for legacy reasons, but I wanted to ensure we can at least track everything in a central place
 * to ease the management of them.
 */
public enum SharedPreferenceDefinitions {

	SmartReceiptsCoreSettings("SmartReceiptsPrefFile", false),
	LastTripTracker("ReceiptsFragment.xml", true),
	Flex("Flex_Preferences", false),
    GoogleDriveSyncMetaData("prefs_google_drive.xml", true),
	SubclassAds("SubClassPrefs", false);

	private final String name;
    private final boolean clearOnPartialWipe;

	SharedPreferenceDefinitions(@NonNull String name, boolean clearOnPartialWipe) {
		this.name = Preconditions.checkNotNull(name);
        this.clearOnPartialWipe = clearOnPartialWipe;
	}

	@NonNull
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

    /**
     * On newer devices (ie Android M and above), Google will attempt to perform an automatic backups
     * as part of the default configuration. While this is generally pretty cool, there are a few major
     * cons with respect to how we handle auto-backups to our Drive store:
     * <ul>
     * <li>The user uploads the database and Google Drive metadata</li>
     * <li>The user then restores the app</li>
     * <li>In some situations, shared preferences were synced but not the database</li>
     * <li>As a result, we attempt to restore the drive metadata and an empty database. We then upload
     * the empty database, which causes the user to lose his/her data.</li>
     * </ul>
     *
     * @param context the current {@link Context}
     */
	public static void clearPreferencesThatCanBeCleared(@NonNull Context context) {
        final SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        defaultPreferences.edit().clear().apply();

        for (final SharedPreferenceDefinitions definition : values()) {
            if (definition.clearOnPartialWipe) {
                context.getSharedPreferences(definition.name, Context.MODE_PRIVATE).edit().clear().apply();
            }
        }
    }

}

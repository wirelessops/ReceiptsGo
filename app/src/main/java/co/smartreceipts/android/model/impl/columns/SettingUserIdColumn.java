package co.smartreceipts.android.model.impl.columns;

import android.support.annotation.NonNull;

import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns blank values for everything but the header
 */
public final class SettingUserIdColumn<T> extends AbstractColumnImpl<T> {

    private final UserPreferenceManager mPreferences;

    public SettingUserIdColumn(int id, @NonNull String name, @NonNull SyncState syncState, @NonNull UserPreferenceManager preferences, int customOrderId) {
        super(id, name, syncState, customOrderId);
        mPreferences = preferences;
    }

    @Override
    public String getValue(@NonNull T rowItem) {
        return mPreferences.get(UserPreference.ReportOutput.UserId);
    }

}

package co.smartreceipts.android.sync.drive.device;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.sql.Date;

import co.smartreceipts.core.sync.model.impl.Identifier;

public class GoogleDriveSyncMetadata {

    private static final String PREFS_GOOGLE_DRIVE = "prefs_google_drive.xml";
    private static final String KEY_DEVICE_IDENTIFIER = "key_device_identifier";
    private static final String KEY_DRIVE_DATABASE_IDENTIFIER = "key_drive_database_identifier";
    private static final String KEY_DRIVE_LAST_SYNC = "key_drive_last_sync";

    private final SharedPreferences mSharedPreferences;
    private final DeviceMetadata mDeviceMetadata;

    public GoogleDriveSyncMetadata(@NonNull Context context) {
        this(context.getSharedPreferences(PREFS_GOOGLE_DRIVE, Context.MODE_PRIVATE), new DeviceMetadata(context));
    }

    private GoogleDriveSyncMetadata(@NonNull SharedPreferences sharedPreferences, @NonNull DeviceMetadata deviceMetadata) {
        mSharedPreferences = Preconditions.checkNotNull(sharedPreferences);
        mDeviceMetadata = Preconditions.checkNotNull(deviceMetadata);
    }

    @NonNull
    public synchronized Identifier getDeviceIdentifier() {
        final String id = mSharedPreferences.getString(KEY_DEVICE_IDENTIFIER, null);
        if (id != null) {
            return new Identifier(id);
        } else {
            final String uniqueDeviceId = mDeviceMetadata.getUniqueDeviceId();
            mSharedPreferences.edit().putString(KEY_DEVICE_IDENTIFIER, uniqueDeviceId).apply();
            return new Identifier(uniqueDeviceId);
        }
    }

    @Nullable
    public synchronized Identifier getDatabaseSyncIdentifier() {
        final String id = mSharedPreferences.getString(KEY_DRIVE_DATABASE_IDENTIFIER, null);
        if (id != null) {
            return new Identifier(id);
        } else {
            return null;
        }
    }

    public synchronized void setDatabaseSyncIdentifier(@NonNull Identifier databaseSyncIdentifier) {
        Preconditions.checkNotNull(databaseSyncIdentifier);
        mSharedPreferences.edit().putString(KEY_DRIVE_DATABASE_IDENTIFIER, databaseSyncIdentifier.getId()).apply();
        mSharedPreferences.edit().putLong(KEY_DRIVE_LAST_SYNC, System.currentTimeMillis()).apply();
    }

    @NonNull
    public synchronized Date getLastDatabaseSyncTime() {
        final long syncTime = mSharedPreferences.getLong(KEY_DRIVE_LAST_SYNC, 0L);
        return new Date(syncTime);
    }

    public void clear() {
        mSharedPreferences.edit().clear().apply();
    }

}

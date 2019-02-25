package co.smartreceipts.android.persistence;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.settings.UserPreferenceManager;
import wb.android.storage.InternalStorageManager;
import wb.android.storage.SDCardFileManager;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;

/**
 * @deprecated in favor of just using Dagger2 to inject these dependencies where appropriate
 */
@ApplicationScope
@Deprecated
public class PersistenceManager {

	private final Context context;
	private final UserPreferenceManager preferenceManager;
    private final StorageManager storageManager;
	private final DatabaseHelper database;

	private SDCardFileManager mExternalStorageManager;
	private InternalStorageManager mInternalStorageManager;

	@Inject
    public PersistenceManager(@NonNull Context context,
                              @NonNull UserPreferenceManager preferenceManager,
                              @NonNull StorageManager storageManager,
                              @NonNull DatabaseHelper database) {
        this.context = Preconditions.checkNotNull(context);
        this.preferenceManager = Preconditions.checkNotNull(preferenceManager);
        this.storageManager = Preconditions.checkNotNull(storageManager);
        this.database = Preconditions.checkNotNull(database);
    }

	public DatabaseHelper getDatabase() {
		return database;
	}

	public StorageManager getStorageManager() {
		return storageManager;
	}

	public SDCardFileManager getExternalStorageManager() throws SDCardStateException {
		if (mExternalStorageManager == null) {
			if (storageManager instanceof SDCardFileManager) {
				mExternalStorageManager = (SDCardFileManager) storageManager;
			}
			else {
				mExternalStorageManager = StorageManager.getExternalInstance(context);
			}
		}
		return mExternalStorageManager;
	}

	public InternalStorageManager getInternalStorageManager() {
		if (mInternalStorageManager == null) {
			if (storageManager instanceof InternalStorageManager) {
				mInternalStorageManager = (InternalStorageManager) storageManager;
			}
			else {
				mInternalStorageManager = StorageManager.getInternalInstance(context);
			}
		}
		return mInternalStorageManager;
	}

    @NonNull
    public UserPreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

}
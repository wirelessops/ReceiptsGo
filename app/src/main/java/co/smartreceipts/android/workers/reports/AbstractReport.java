package co.smartreceipts.android.workers.reports;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import wb.android.storage.StorageManager;

/**
 * Provides some core behavior that multiple {@link Report} implementations use
 */
public abstract class AbstractReport implements Report {

    private final ReportResourcesManager reportResourcesManager;
    private final DatabaseHelper databaseHelper;
    private final UserPreferenceManager userPreferenceManager;
    private final StorageManager storageManager;

    protected AbstractReport(@NonNull ReportResourcesManager reportResourcesManager,
                             @NonNull PersistenceManager persistenceManager) {
        this(reportResourcesManager, persistenceManager.getDatabase(),
                persistenceManager.getPreferenceManager(), persistenceManager.getStorageManager());
    }

    protected AbstractReport(@NonNull ReportResourcesManager reportResourcesManager,
                             @NonNull DatabaseHelper db, @NonNull UserPreferenceManager preferences,
                             @NonNull StorageManager storageManager) {
        this.reportResourcesManager = Preconditions.checkNotNull(reportResourcesManager);
        this.databaseHelper = Preconditions.checkNotNull(db);
        this.userPreferenceManager = Preconditions.checkNotNull(preferences);
        this.storageManager = Preconditions.checkNotNull(storageManager);
    }

    @NonNull
    protected final ReportResourcesManager getReportResourcesManager() {
        return reportResourcesManager;
    }

    @NonNull
    protected final DatabaseHelper getDatabase() {
        return databaseHelper;
    }

    @NonNull
    protected final UserPreferenceManager getPreferences() {
        return userPreferenceManager;
    }

    @NonNull
    protected final StorageManager getStorageManager() {
        return storageManager;
    }
}
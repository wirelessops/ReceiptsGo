package co.smartreceipts.android.persistence.database.restore;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.io.File;

import co.smartreceipts.android.database.DatabaseContext;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Single;
import wb.android.storage.StorageManager;

/**
 * Opens a SQL Handle for a known Smart Receipts database file
 */
class ImportedDatabaseFetcher {

    private final DatabaseContext context;
    private final StorageManager storageManager;
    private final UserPreferenceManager preferences;
    private final ReceiptColumnDefinitions receiptColumnDefinitions;
    private final TableDefaultsCustomizer tableDefaultsCustomizer;
    private final OrderingPreferencesManager orderingPreferencesManager;

    public ImportedDatabaseFetcher(@NonNull DatabaseContext context,
                                   @NonNull StorageManager storageManager,
                                   @NonNull UserPreferenceManager preferences,
                                   @NonNull ReceiptColumnDefinitions receiptColumnDefinitions,
                                   @NonNull TableDefaultsCustomizer tableDefaultsCustomizer,
                                   @NonNull OrderingPreferencesManager orderingPreferencesManager) {
        this.context = Preconditions.checkNotNull(context);
        this.storageManager = Preconditions.checkNotNull(storageManager);
        this.preferences = Preconditions.checkNotNull(preferences);
        this.receiptColumnDefinitions = Preconditions.checkNotNull(receiptColumnDefinitions);
        this.tableDefaultsCustomizer = Preconditions.checkNotNull(tableDefaultsCustomizer);
        this.orderingPreferencesManager = Preconditions.checkNotNull(orderingPreferencesManager);
    }

    /**
     * Attempts to open a handle to a SQLite database from an input database file
     *
     * @param databaseFile the database file
     * @return a {@link Single}, which will emit the databaseß if successfully opened
     */
    @NonNull
    public Single<DatabaseHelper> getDatabase(@NonNull File databaseFile) {
        return Single.fromCallable(() -> {
            Logger.debug(ImportedDatabaseFetcher.this, "Attempting to acquire a handle to (and possibly update) our import database");
            final DatabaseHelper databaseHelper = new DatabaseHelper(context, storageManager, preferences, receiptColumnDefinitions, tableDefaultsCustomizer, orderingPreferencesManager, Optional.of(databaseFile.getAbsolutePath()));
            Logger.debug(ImportedDatabaseFetcher.this, "Successfully acquired a handle to the import database");
            return databaseHelper;
        });
    }
}

package co.smartreceipts.android.persistence.database.restore;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.File;

import javax.inject.Inject;

import co.smartreceipts.android.database.DatabaseContext;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.analytics.log.Logger;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import wb.android.storage.StorageManager;

/**
 * When a user attempts to restore his/her content from an existing backup, we need a mechanism in
 * which he/she can easily "merge" the old content with the new. This class is dedicated to this
 * process to ensure that our users have an intuitive means of recovery.
 */
@ApplicationScope
public class DatabaseRestorer {

    private final DatabaseHelper databaseHelper;
    private final ImportedDatabaseFetcher importedDatabaseFetcher;
    private final DatabaseMergerFactory databaseMergerFactory;

    @Inject
    DatabaseRestorer(@NonNull DatabaseContext context,
                     @NonNull DatabaseHelper databaseHelper,
                     @NonNull StorageManager storageManager,
                     @NonNull UserPreferenceManager preferences,
                     @NonNull ReceiptColumnDefinitions receiptColumnDefinitions,
                     @NonNull TableDefaultsCustomizer tableDefaultsCustomizer,
                     @NonNull OrderingPreferencesManager orderingPreferencesManager) {
        this(databaseHelper, new ImportedDatabaseFetcher(context, storageManager, preferences, receiptColumnDefinitions, tableDefaultsCustomizer, orderingPreferencesManager), new DatabaseMergerFactory());
    }

    private DatabaseRestorer(@NonNull DatabaseHelper databaseHelper,
                             @NonNull ImportedDatabaseFetcher importedDatabaseFetcher,
                             @NonNull DatabaseMergerFactory databaseMergerFactory) {
        this.databaseHelper = Preconditions.checkNotNull(databaseHelper);
        this.importedDatabaseFetcher = Preconditions.checkNotNull(importedDatabaseFetcher);
        this.databaseMergerFactory = Preconditions.checkNotNull(databaseMergerFactory);
    }

    /**
     * Restores the database of a constructor-provided import file path, so that database data is
     * now reflected in our current file-system
     *
     * @param importedDatabaseBackupFile the {@link File}, containing the database to restore
     * @param overwriteExistingData if we should overwrite our existing data or not
     *
     * @return a {@link Completable} that will emit {@link CompletableEmitter#onComplete()} if this
     * process completed successfully or {@link CompletableEmitter#onError(Throwable)} if not
     */
    @NonNull
    public Completable restoreDatabase(@NonNull File importedDatabaseBackupFile, boolean overwriteExistingData) {
        return importedDatabaseFetcher.getDatabase(importedDatabaseBackupFile)
                .flatMapCompletable(importedBackupDatabase -> {
                    final DatabaseMerger databaseMerger = databaseMergerFactory.get(overwriteExistingData);

                    return databaseMerger.merge(databaseHelper, importedBackupDatabase)
                            .doOnSubscribe(ignored -> {
                                Logger.info(DatabaseRestorer.this, "Beginning import process with {}", databaseMerger.getClass().getSimpleName());
                                databaseHelper.getWritableDatabase().beginTransaction();
                            })
                            .doOnComplete(() -> {
                                Logger.info(DatabaseRestorer.this, "Successfully completed the import process");
                                databaseHelper.getWritableDatabase().setTransactionSuccessful();
                                databaseHelper.getWritableDatabase().endTransaction();
                                for (final Table table : databaseHelper.getTables()) {
                                    // Clear all our of in-memory caches
                                    table.clearCache();
                                }
                            })
                            .doOnError(error -> {
                                Logger.info(DatabaseRestorer.this, "Failed to import your data", error);
                                databaseHelper.getWritableDatabase().endTransaction();
                            });
                });
    }
}

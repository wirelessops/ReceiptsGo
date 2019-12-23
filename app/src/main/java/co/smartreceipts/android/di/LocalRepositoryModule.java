package co.smartreceipts.android.di;

import android.content.Context;

import co.smartreceipts.android.database.DatabaseContext;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import dagger.Module;
import dagger.Provides;
import wb.android.storage.StorageManager;

@Module
public class LocalRepositoryModule {

    @Provides
    @ApplicationScope
    public static StorageManager provideStorageManager(Context context) {
        final StorageManager storageManager = StorageManager.getInstance(context);
        storageManager.initialize(); // TODO: Move all database calls off the UI thread to remove this requirement
        return storageManager;
    }

    @Provides
    @ApplicationScope
    public static DatabaseHelper provideDatabaseHelper(DatabaseContext context,
                                                       StorageManager storageManager,
                                                       UserPreferenceManager preferences,
                                                       ReceiptColumnDefinitions receiptColumnDefinitions,
                                                       TableDefaultsCustomizer tableDefaultsCustomizer,
                                                       OrderingPreferencesManager orderingPreferencesManager) {
        return DatabaseHelper.getInstance(context, storageManager, preferences, receiptColumnDefinitions,
                tableDefaultsCustomizer, orderingPreferencesManager);
    }

    @Provides
    @ApplicationScope
    public static ColumnDefinitions<Receipt> provideColumnDefinitionReceipts(ReceiptColumnDefinitions receiptColumnDefinitions) {
        return receiptColumnDefinitions;
    }
}

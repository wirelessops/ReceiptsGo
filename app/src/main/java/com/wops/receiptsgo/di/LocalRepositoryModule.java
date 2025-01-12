package com.wops.receiptsgo.di;

import android.content.Context;

import com.wops.receiptsgo.database.DatabaseContext;
import com.wops.core.di.scopes.ApplicationScope;
import com.wops.receiptsgo.model.ColumnDefinitions;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions;
import com.wops.receiptsgo.persistence.DatabaseHelper;
import com.wops.receiptsgo.persistence.database.defaults.TableDefaultsCustomizer;
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderingPreferencesManager;
import com.wops.receiptsgo.settings.UserPreferenceManager;
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

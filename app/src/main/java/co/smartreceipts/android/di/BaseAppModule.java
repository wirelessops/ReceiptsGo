package co.smartreceipts.android.di;

import android.content.Context;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder;
import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.apis.hosts.SmartReceiptsHostConfiguration;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.config.DefaultConfigurationManager;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.identity.store.MutableIdentityStore;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;
import co.smartreceipts.android.persistence.database.defaults.WhiteLabelFriendlyTableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.rating.data.AppRatingPreferencesStorage;
import co.smartreceipts.android.rating.data.AppRatingStorage;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.widget.tooltip.report.backup.data.BackupReminderPreferencesStorage;
import co.smartreceipts.android.widget.tooltip.report.backup.data.BackupReminderTooltipStorage;
import co.smartreceipts.android.widget.tooltip.report.generate.data.GenerateInfoTooltipPreferencesStorage;
import co.smartreceipts.android.widget.tooltip.report.generate.data.GenerateInfoTooltipStorage;
import dagger.Module;
import dagger.Provides;
import wb.android.flex.Flex;
import wb.android.flex.Flexable;
import wb.android.storage.StorageManager;

@Module
public class BaseAppModule {

    private final SmartReceiptsApplication application;

    public BaseAppModule(SmartReceiptsApplication application) {
        this.application = application;
    }

    @Provides
    @ApplicationScope
    Context provideContext() {
        return application;
    }


    @Provides
    @ApplicationScope
    public static Flex provideFlex(Context context) {
        return new Flex(context, new Flexable() {
            @Override
            public int getFleXML() {
                return Flexable.UNDEFINED;
            }
        });
    }

    @Provides
    @ApplicationScope
    public static StorageManager provideStorageManager(Context context) {
        return StorageManager.getInstance(context);
    }

    @Provides
    @ApplicationScope
    public static DatabaseHelper provideDatabaseHelper(Context context, StorageManager storageManager,
                                                       UserPreferenceManager preferences,
                                                       ReceiptColumnDefinitions receiptColumnDefinitions,
                                                       WhiteLabelFriendlyTableDefaultsCustomizer tableDefaultsCustomizer,
                                                       OrderingPreferencesManager orderingPreferencesManager) {
        return DatabaseHelper.getInstance(context, storageManager, preferences, receiptColumnDefinitions,
                tableDefaultsCustomizer, orderingPreferencesManager);
    }

    @Provides
    @ApplicationScope
    public static ConfigurationManager provideConfigurationManager(DefaultConfigurationManager manager) {
        return manager;
    }

    @Provides
    @ApplicationScope
    public static AppRatingStorage provideAppRatingStorage(AppRatingPreferencesStorage storage) {
        return storage;
    }

    @Provides
    @co.smartreceipts.android.di.qualifiers.ReceiptColumnDefinitions
    public static ColumnDefinitions<Receipt> provideColumnDefinitionReceipts(ReceiptColumnDefinitions receiptColumnDefinitions) {
        return receiptColumnDefinitions;
    }

    @Provides
    @co.smartreceipts.android.di.qualifiers.TripTableController
    public static TableController<Trip> provideTripTableController (TripTableController tripTableController) {
        return tripTableController;
    }

    @Provides
    @ApplicationScope
    public static ServiceManager provideServiceManager(MutableIdentityStore mutableIdentityStore,
                                                       ReceiptColumnDefinitions receiptColumnDefinitions) {
        return new ServiceManager(new SmartReceiptsHostConfiguration(mutableIdentityStore,
                new SmartReceiptsGsonBuilder(receiptColumnDefinitions)));
    }

    @Provides
    @ApplicationScope
    public static GenerateInfoTooltipStorage provideGenerateInfoTooltipStorage(GenerateInfoTooltipPreferencesStorage storage) {
        return storage;
    }

    @Provides
    @ApplicationScope
    public static BackupReminderTooltipStorage provideBackupReminderTooltipStorage (BackupReminderPreferencesStorage storage) {
        return storage;
    }
}

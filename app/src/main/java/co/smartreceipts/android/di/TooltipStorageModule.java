package co.smartreceipts.android.di;


import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.rating.data.AppRatingPreferencesStorage;
import co.smartreceipts.android.rating.data.AppRatingStorage;
import co.smartreceipts.android.widget.tooltip.report.backup.data.BackupReminderPreferencesStorage;
import co.smartreceipts.android.widget.tooltip.report.backup.data.BackupReminderTooltipStorage;
import co.smartreceipts.android.widget.tooltip.report.generate.data.GenerateInfoTooltipPreferencesStorage;
import co.smartreceipts.android.widget.tooltip.report.generate.data.GenerateInfoTooltipStorage;
import dagger.Module;
import dagger.Provides;

@Module
public class TooltipStorageModule {

    @Provides
    @ApplicationScope
    public static GenerateInfoTooltipStorage provideGenerateInfoTooltipStorage(GenerateInfoTooltipPreferencesStorage storage) {
        return storage;
    }

    @Provides
    @ApplicationScope
    public static BackupReminderTooltipStorage provideBackupReminderTooltipStorage(BackupReminderPreferencesStorage storage) {
        return storage;
    }

    @Provides
    @ApplicationScope
    public static AppRatingStorage provideAppRatingStorage(AppRatingPreferencesStorage storage) {
        return storage;
    }

}

package com.wops.receiptsgo.di;


import com.wops.core.di.scopes.ApplicationScope;
import com.wops.receiptsgo.rating.data.AppRatingPreferencesStorage;
import com.wops.receiptsgo.rating.data.AppRatingStorage;
import com.wops.receiptsgo.widget.tooltip.report.backup.data.BackupReminderPreferencesStorage;
import com.wops.receiptsgo.widget.tooltip.report.backup.data.BackupReminderTooltipStorage;
import com.wops.receiptsgo.widget.tooltip.report.generate.data.GenerateInfoTooltipPreferencesStorage;
import com.wops.receiptsgo.widget.tooltip.report.generate.data.GenerateInfoTooltipStorage;
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

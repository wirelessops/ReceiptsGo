package com.wops.receiptsgo.di;

import co.smartreceipts.analytics.Analytics;
import com.wops.receiptsgo.activities.FragmentProvider;
import com.wops.receiptsgo.activities.NavigationHandler;
import com.wops.receiptsgo.activities.SmartReceiptsActivity;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.receiptsgo.widget.tooltip.report.ReportTooltipInteractor;
import com.wops.receiptsgo.widget.tooltip.report.backup.BackupReminderTooltipManager;
import com.wops.receiptsgo.widget.tooltip.report.generate.GenerateInfoTooltipManager;
import com.wops.receiptsgo.widget.tooltip.report.intent.ImportInfoTooltipManager;
import co.smartreceipts.core.di.scopes.ActivityScope;
import dagger.Module;
import dagger.Provides;

@Module
public class SmartReceiptsActivityModule {

    @ActivityScope
    @Provides
    NavigationHandler provideNavigationHandler(SmartReceiptsActivity activity, FragmentProvider fragmentProvider) {
        return new NavigationHandler<>(activity, fragmentProvider);
    }

    @ActivityScope
    @Provides
    ReportTooltipInteractor provideReportTooltipInteractor(SmartReceiptsActivity activity,
                                                           NavigationHandler navigationHandler,
                                                           BackupProvidersManager backupProvidersManager,
                                                           Analytics analytics,
                                                           GenerateInfoTooltipManager generateInfoTooltipManager,
                                                           BackupReminderTooltipManager backupReminderTooltipManager,
                                                           ImportInfoTooltipManager importInfoTooltipManager) {
        return new ReportTooltipInteractor<>(activity, navigationHandler, backupProvidersManager,
                analytics, generateInfoTooltipManager, backupReminderTooltipManager, importInfoTooltipManager);
    }
}
package com.wops.receiptsgo.di;

import com.wops.analytics.Analytics;
import com.wops.receiptsgo.activities.FragmentProvider;
import com.wops.receiptsgo.activities.NavigationHandler;
import com.wops.receiptsgo.activities.ReceiptsGoActivity;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.receiptsgo.widget.tooltip.report.ReportTooltipInteractor;
import com.wops.receiptsgo.widget.tooltip.report.backup.BackupReminderTooltipManager;
import com.wops.receiptsgo.widget.tooltip.report.generate.GenerateInfoTooltipManager;
import com.wops.receiptsgo.widget.tooltip.report.intent.ImportInfoTooltipManager;
import com.wops.core.di.scopes.ActivityScope;
import dagger.Module;
import dagger.Provides;

@Module
public class ReceiptsGoActivityModule {

    @ActivityScope
    @Provides
    NavigationHandler provideNavigationHandler(ReceiptsGoActivity activity, FragmentProvider fragmentProvider) {
        return new NavigationHandler<>(activity, fragmentProvider);
    }

    @ActivityScope
    @Provides
    ReportTooltipInteractor provideReportTooltipInteractor(ReceiptsGoActivity activity,
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
package co.smartreceipts.android.di;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.android.activities.FragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.widget.tooltip.report.ReportTooltipInteractor;
import co.smartreceipts.android.widget.tooltip.report.backup.BackupReminderTooltipManager;
import co.smartreceipts.android.widget.tooltip.report.generate.GenerateInfoTooltipManager;
import co.smartreceipts.android.widget.tooltip.report.intent.ImportInfoTooltipManager;
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
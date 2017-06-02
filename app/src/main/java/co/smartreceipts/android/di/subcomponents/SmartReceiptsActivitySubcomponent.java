package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.activities.FragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.di.SmartReceiptsActivityAdModule;
import co.smartreceipts.android.di.SmartReceiptsActivityBindingModule;
import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.widget.tooltip.report.ReportTooltipInteractor;
import co.smartreceipts.android.widget.tooltip.report.generate.GenerateInfoTooltipManager;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScope
@Subcomponent(modules = {
        SmartReceiptsActivitySubcomponent.SmartReceiptsActivityModule.class,
        SmartReceiptsActivityBindingModule.class,
        SmartReceiptsActivityAdModule.class
})
public interface SmartReceiptsActivitySubcomponent extends AndroidInjector<SmartReceiptsActivity> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<SmartReceiptsActivity> {
    }

    @Module
    class SmartReceiptsActivityModule {

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
                                                               GenerateInfoTooltipManager tooltipManager) {
            return new ReportTooltipInteractor<>(activity, navigationHandler, backupProvidersManager,
                    analytics, tooltipManager);
        }
    }
}

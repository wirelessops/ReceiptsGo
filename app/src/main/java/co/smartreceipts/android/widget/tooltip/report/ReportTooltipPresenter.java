package co.smartreceipts.android.widget.tooltip.report;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.sync.BackupProviderChangeListener;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.tooltip.TooltipView;
import co.smartreceipts.android.widget.viper.BaseViperPresenter;

@FragmentScope
public class ReportTooltipPresenter extends BaseViperPresenter<TooltipView, ReportTooltipInteractor<? extends FragmentActivity>> implements BackupProviderChangeListener {

    private final BackupProvidersManager backupProvidersManager;
    private final Analytics analytics;

    @SuppressWarnings("unchecked")
    @Inject
    public ReportTooltipPresenter(@NonNull TooltipView view, @NonNull ReportTooltipInteractor interactor,
                                  @NonNull BackupProvidersManager backupProvidersManager,
                                  @NonNull Analytics analytics) {
        super(view, interactor);

        this.backupProvidersManager = Preconditions.checkNotNull(backupProvidersManager);
        this.analytics = Preconditions.checkNotNull(analytics);
    }

    @Override
    public void subscribe() {
        backupProvidersManager.registerChangeListener(this);
        updateProvider(backupProvidersManager.getSyncProvider());

        compositeDisposable.add(interactor.checkTooltipCauses()
                .subscribe(view::present));

        compositeDisposable.add(view.getTooltipsClicks()
                .doOnNext(uiIndicator -> {
                    Logger.info(ReportTooltipPresenter.this, "User clicked on {} tooltip", uiIndicator);
                    if (uiIndicator.getState() == ReportTooltipUiIndicator.State.GenerateInfo) {
                        analytics.record(Events.Informational.ClickedGenerateReportTip);
                    } else if (uiIndicator.getState() == ReportTooltipUiIndicator.State.BackupReminder) {
                        analytics.record(Events.Informational.ClickedBackupReminderTip);
                    }
                })
                .subscribe(uiIndicator -> {
                    view.present(ReportTooltipUiIndicator.none());
                    if (uiIndicator.getState() == ReportTooltipUiIndicator.State.SyncError) {
                        interactor.handleClickOnErrorTooltip(uiIndicator.getErrorType().get());
                    } else if (uiIndicator.getState() == ReportTooltipUiIndicator.State.GenerateInfo) {
                        // Note: The actual click logic is in the view (probably need to clean up dagger for this to be cleaner)
                        interactor.generateInfoTooltipClosed();
                    } else if (uiIndicator.getState() == ReportTooltipUiIndicator.State.BackupReminder) {
                        interactor.backupReminderTooltipClosed();
                    }
                }));

        compositeDisposable.add(view.getCloseButtonClicks()
                .subscribe(uiIndicator -> {
                    view.present(ReportTooltipUiIndicator.none());
                    if (uiIndicator.getState() == ReportTooltipUiIndicator.State.GenerateInfo) {
                        interactor.generateInfoTooltipClosed();
                    } else if (uiIndicator.getState() == ReportTooltipUiIndicator.State.BackupReminder) {
                        interactor.backupReminderTooltipClosed();
                    } else if (uiIndicator.getState() == ReportTooltipUiIndicator.State.ImportInfo) {
                        interactor.importInfoTooltipClosed();
                    }
                }));
    }

    @Override
    public void unsubscribe() {
        super.unsubscribe();

        backupProvidersManager.unregisterChangeListener(this);
    }

    @Override
    public void onProviderChanged(@NonNull SyncProvider newProvider) {
        updateProvider(newProvider);
    }

    private void updateProvider(@NonNull SyncProvider provider) {
        if (provider == SyncProvider.None) {
            view.present(ReportTooltipUiIndicator.none());
        }
    }
}

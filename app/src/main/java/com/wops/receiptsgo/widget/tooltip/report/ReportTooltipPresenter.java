package com.wops.receiptsgo.widget.tooltip.report;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import com.wops.analytics.Analytics;
import com.wops.analytics.events.Events;
import com.wops.core.di.scopes.FragmentScope;
import com.wops.receiptsgo.sync.BackupProviderChangeListener;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.core.sync.provider.SyncProvider;
import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.widget.tooltip.LegacyTooltipView;
import com.wops.receiptsgo.widget.viper.BaseViperPresenter;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@FragmentScope
public class ReportTooltipPresenter extends BaseViperPresenter<LegacyTooltipView, ReportTooltipInteractor<? extends FragmentActivity>> implements BackupProviderChangeListener {

    private final BackupProvidersManager backupProvidersManager;
    private final Analytics analytics;
    private final Scheduler subscribeOnScheduler;
    private final Scheduler observeOnScheduler;

    @SuppressWarnings("unchecked")
    @Inject
    public ReportTooltipPresenter(@NonNull LegacyTooltipView view,
                                  @NonNull ReportTooltipInteractor interactor,
                                  @NonNull BackupProvidersManager backupProvidersManager,
                                  @NonNull Analytics analytics) {
        this(view, interactor, backupProvidersManager, analytics, Schedulers.io(), AndroidSchedulers.mainThread());
    }

    @SuppressWarnings("unchecked")
    public ReportTooltipPresenter(@NonNull LegacyTooltipView view,
                                  @NonNull ReportTooltipInteractor interactor,
                                  @NonNull BackupProvidersManager backupProvidersManager,
                                  @NonNull Analytics analytics,
                                  @NonNull Scheduler subscribeOnScheduler,
                                  @NonNull Scheduler observeOnScheduler) {
        super(view, interactor);
        this.backupProvidersManager = Preconditions.checkNotNull(backupProvidersManager);
        this.analytics = Preconditions.checkNotNull(analytics);
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
        this.observeOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
    }

    @Override
    public void subscribe() {
        backupProvidersManager.registerChangeListener(this);
        updateProvider(backupProvidersManager.getSyncProvider());

        compositeDisposable.add(interactor.checkTooltipCauses()
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
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

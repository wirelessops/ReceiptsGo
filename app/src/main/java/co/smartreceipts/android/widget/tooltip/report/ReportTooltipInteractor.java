package co.smartreceipts.android.widget.tooltip.report;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.errors.CriticalSyncError;
import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.sync.widget.errors.DriveRecoveryDialogFragment;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.tooltip.report.backup.BackupReminderTooltipManager;
import co.smartreceipts.android.widget.tooltip.report.generate.GenerateInfoTooltipManager;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

@ActivityScope
public class ReportTooltipInteractor<T extends FragmentActivity> {

    private final FragmentActivity fragmentActivity;
    private final NavigationHandler navigationHandler;
    private final BackupProvidersManager backupProvidersManager;
    private final Analytics analytics;
    private final GenerateInfoTooltipManager generateInfoTooltipManager;
    private final BackupReminderTooltipManager backupReminderTooltipManager;

    @Inject
    public ReportTooltipInteractor(T activity, NavigationHandler navigationHandler,
                                   BackupProvidersManager backupProvidersManager,
                                   Analytics analytics, GenerateInfoTooltipManager infoTooltipManager,
                                   BackupReminderTooltipManager backupReminderTooltipManager) {
        this.fragmentActivity = activity;
        this.navigationHandler = navigationHandler;
        this.backupProvidersManager = backupProvidersManager;
        this.analytics = analytics;
        this.generateInfoTooltipManager = infoTooltipManager;
        this.backupReminderTooltipManager = backupReminderTooltipManager;
    }

    public Observable<ReportTooltipUiIndicator> checkTooltipCauses() {

        Observable<ReportTooltipUiIndicator> infoIndicatorObservable = Observable.concat(
                needToShowGenerateInfo()
                        .flatMapObservable(needToShow -> needToShow ? Observable.just(ReportTooltipUiIndicator.generateInfo())
                                : Observable.just(ReportTooltipUiIndicator.none())),
                needToShowBackupReminder()
                        .flatMapObservable(days -> Observable.just(ReportTooltipUiIndicator.backupReminder(days))))
                .toList()
                .flatMapObservable(
                        reportTooltipUiIndicators -> {
                            for (ReportTooltipUiIndicator reportTooltipUiIndicator : reportTooltipUiIndicators) {
                                // this makes priority for backup reminder stream
                                if (reportTooltipUiIndicator.getState() == ReportTooltipUiIndicator.State.BackupReminder) {
                                    return Observable.just(reportTooltipUiIndicator);
                                }
                            }
                            return Observable.just(reportTooltipUiIndicators.get(0));
                        });

        return Observable.combineLatest(
                getErrorStream()
                        .doOnNext(syncErrorType -> { // it must start with some element because actual source observable can be empty() or never()
                            analytics.record(new DefaultDataPointEvent(Events.Sync.DisplaySyncError)
                                    .addDataPoint(new DataPoint(SyncErrorType.class.getName(), syncErrorType)));
                            Logger.info(this, "Received sync error: {}.", syncErrorType);
                        })
                        .flatMap(syncErrorType -> Observable.just(ReportTooltipUiIndicator.syncError(syncErrorType)))
                        .startWith(ReportTooltipUiIndicator.none()),
                infoIndicatorObservable,
                (errorUiIndicator, infoUiIndicator) -> {
                    if (errorUiIndicator.equals(ReportTooltipUiIndicator.none())) { // this makes some kind of priority for errors stream
                        return infoUiIndicator;
                    } else {
                        return errorUiIndicator;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<SyncErrorType> getErrorStream() {
        return backupProvidersManager.getCriticalSyncErrorStream()
                .map(CriticalSyncError::getSyncErrorType);
    }

    private Single<Boolean> needToShowGenerateInfo() {
        return generateInfoTooltipManager.needToShowGenerateTooltip();
    }

    private Maybe<Integer> needToShowBackupReminder() {
        return backupReminderTooltipManager.needToShowBackupReminder();
    }

    public void handleClickOnErrorTooltip(@NonNull SyncErrorType syncErrorType) {
        final SyncProvider syncProvider = backupProvidersManager.getSyncProvider();
        Preconditions.checkArgument(syncProvider == SyncProvider.GoogleDrive, "Only Google Drive clicks are supported");

        analytics.record(new DefaultDataPointEvent(Events.Sync.ClickSyncError)
                .addDataPoint(new DataPoint(SyncErrorType.class.getName(), syncErrorType)));

        Logger.info(this, "Handling click for sync error: {}.", syncErrorType);

        if (syncErrorType == SyncErrorType.NoRemoteDiskSpace) {
            backupProvidersManager.markErrorResolved(syncErrorType);

        } else if (syncErrorType == SyncErrorType.UserDeletedRemoteData) {
            navigationHandler.showDialog(new DriveRecoveryDialogFragment());

        } else if (syncErrorType == SyncErrorType.UserRevokedRemoteRights) {
            backupProvidersManager.initialize(fragmentActivity);
            backupProvidersManager.markErrorResolved(syncErrorType);

        } else {
            throw new IllegalArgumentException("Unknown SyncErrorType");
        }
    }

    public void generateInfoTooltipClosed() {
        generateInfoTooltipManager.tooltipWasDismissed();
    }

    public void backupReminderTooltipClosed() {
        backupReminderTooltipManager.tooltipWasDismissed();
    }

}

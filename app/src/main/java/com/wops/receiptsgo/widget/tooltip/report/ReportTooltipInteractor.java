package com.wops.receiptsgo.widget.tooltip.report;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.FragmentActivity;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import com.wops.receiptsgo.activities.NavigationHandler;
import com.wops.analytics.Analytics;
import com.wops.analytics.events.DataPoint;
import com.wops.analytics.events.DefaultDataPointEvent;
import com.wops.analytics.events.Events;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.core.sync.errors.CriticalSyncError;
import com.wops.core.sync.errors.SyncErrorType;
import com.wops.core.sync.provider.SyncProvider;
import com.wops.receiptsgo.sync.widget.errors.DriveRecoveryDialogFragment;
import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.widget.tooltip.report.backup.BackupReminderTooltipManager;
import com.wops.receiptsgo.widget.tooltip.report.generate.GenerateInfoTooltipManager;
import com.wops.receiptsgo.widget.tooltip.report.intent.ImportInfoTooltipManager;
import com.wops.core.di.scopes.ActivityScope;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.wops.receiptsgo.widget.tooltip.report.ReportTooltipUiIndicator.State.None;

@ActivityScope
public class ReportTooltipInteractor<T extends FragmentActivity> {

    private final FragmentActivity fragmentActivity;
    private final NavigationHandler navigationHandler;
    private final BackupProvidersManager backupProvidersManager;
    private final Analytics analytics;
    private final GenerateInfoTooltipManager generateInfoTooltipManager;
    private final BackupReminderTooltipManager backupReminderTooltipManager;
    private final ImportInfoTooltipManager importInfoTooltipManager;
    private final Scheduler observeOnScheduler;

    @Inject
    public ReportTooltipInteractor(T activity, NavigationHandler navigationHandler,
                                   BackupProvidersManager backupProvidersManager,
                                   Analytics analytics, GenerateInfoTooltipManager infoTooltipManager,
                                   BackupReminderTooltipManager backupReminderTooltipManager,
                                   ImportInfoTooltipManager importInfoTooltipManager) {
        this(activity, navigationHandler, backupProvidersManager, analytics, infoTooltipManager, backupReminderTooltipManager, importInfoTooltipManager, AndroidSchedulers.mainThread());
    }

    @VisibleForTesting
    ReportTooltipInteractor(T activity, NavigationHandler navigationHandler,
                            BackupProvidersManager backupProvidersManager,
                            Analytics analytics, GenerateInfoTooltipManager infoTooltipManager,
                            BackupReminderTooltipManager backupReminderTooltipManager,
                            ImportInfoTooltipManager importInfoTooltipManager,
                            Scheduler observeOnScheduler) {
        this.fragmentActivity = activity;
        this.navigationHandler = navigationHandler;
        this.backupProvidersManager = backupProvidersManager;
        this.analytics = analytics;
        this.generateInfoTooltipManager = infoTooltipManager;
        this.backupReminderTooltipManager = backupReminderTooltipManager;
        this.importInfoTooltipManager = importInfoTooltipManager;
        this.observeOnScheduler = observeOnScheduler;
    }

    // Tooltips (sorted by priority):
    // 1) errors
    // 2) import info
    // 3) backup reminder
    // 4) generate info
    public Observable<ReportTooltipUiIndicator> checkTooltipCauses() {

        return Observable.combineLatest(
                getErrorStream()
                        .doOnNext(syncErrorType -> { // it must start with some element because actual source observable can be empty() or never()
                            analytics.record(new DefaultDataPointEvent(Events.Sync.DisplaySyncError)
                                    .addDataPoint(new DataPoint(SyncErrorType.class.getName(), syncErrorType)));
                            Logger.info(this, "Received sync error: {}.", syncErrorType);
                        })
                        .flatMap(syncErrorType -> Observable.just(ReportTooltipUiIndicator.syncError(syncErrorType)))
                        .startWith(ReportTooltipUiIndicator.none()),
                getInfoStream(),
                (errorUiIndicator, infoUiIndicator) -> errorUiIndicator.getState() != None ? errorUiIndicator : infoUiIndicator)
                .observeOn(observeOnScheduler);
    }

    private Observable<SyncErrorType> getErrorStream() {
        return backupProvidersManager.getCriticalSyncErrorStream()
                .map(CriticalSyncError::getSyncErrorType);
    }

    /**
     * Checks all info causes (import info, backup reminder, generate info)
     *
     * @return The most important info {@link ReportTooltipUiIndicator}
     */
    private Observable<ReportTooltipUiIndicator> getInfoStream() {
        return Observable.combineLatest(checkImportInfoCauses(),
                checkBackupReminderCauses().toObservable(),
                checkGenerateInfoCauses().toObservable(),
                (importInfoUiIndicator, backupReminderUiIndicator, generateInfoUiIndicator) -> {

                    if (importInfoUiIndicator.getState() != None) { // import info has the highest info priority
                        return importInfoUiIndicator;
                    } else if (backupReminderUiIndicator.getState() != None) { // backup reminder has higher priority than generate info
                        return backupReminderUiIndicator;
                    } else {
                        return generateInfoUiIndicator;
                    }

                });
    }

    private Single<ReportTooltipUiIndicator> checkGenerateInfoCauses() {
        return generateInfoTooltipManager.needToShowGenerateTooltip()
                .flatMap(needToShow -> needToShow ? Single.just(ReportTooltipUiIndicator.generateInfo()) : Single.just(ReportTooltipUiIndicator.none()));
    }

    private Observable<ReportTooltipUiIndicator> checkImportInfoCauses() {
        return importInfoTooltipManager.needToShowImportInfo()
                .map(importIntentPresent -> importIntentPresent ? ReportTooltipUiIndicator.importInfo() : ReportTooltipUiIndicator.none());
    }

    private Single<ReportTooltipUiIndicator> checkBackupReminderCauses() {
        return backupReminderTooltipManager.needToShowBackupReminder()
                .flatMapSingleElement(days -> Single.just(ReportTooltipUiIndicator.backupReminder(days)))
                .toSingle(ReportTooltipUiIndicator.none());
    }

    public void handleClickOnErrorTooltip(@NonNull SyncErrorType syncErrorType) {
        final SyncProvider syncProvider = backupProvidersManager.getSyncProvider();
        Preconditions.checkArgument(syncProvider == SyncProvider.GoogleDrive, "Only Google Drive clicks are supported");

        analytics.record(new DefaultDataPointEvent(Events.Sync.ClickSyncError)
                .addDataPoint(new DataPoint(SyncErrorType.class.getName(), syncErrorType)));

        Logger.info(this, "Handling click for sync error: {}.", syncErrorType);

        if (syncErrorType == SyncErrorType.NoRemoteDiskSpace) {
            backupProvidersManager.markErrorResolved(syncErrorType);

        } else if (syncErrorType == SyncErrorType.DriveRecoveryRequired) {
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

    public void importInfoTooltipClosed() {
        importInfoTooltipManager.tooltipWasDismissed();
    }

}

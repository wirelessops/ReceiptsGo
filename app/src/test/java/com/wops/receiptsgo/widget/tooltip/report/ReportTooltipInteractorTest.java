package com.wops.receiptsgo.widget.tooltip.report;

import androidx.fragment.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import com.wops.receiptsgo.activities.NavigationHandler;
import com.wops.analytics.Analytics;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.core.sync.errors.CriticalSyncError;
import com.wops.core.sync.errors.SyncErrorType;
import com.wops.core.sync.provider.SyncProvider;
import com.wops.receiptsgo.widget.tooltip.report.backup.BackupReminderTooltipManager;
import com.wops.receiptsgo.widget.tooltip.report.generate.GenerateInfoTooltipManager;
import com.wops.receiptsgo.widget.tooltip.report.intent.ImportInfoTooltipManager;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportTooltipInteractorTest {

    private static final int DAYS = 20;

    //Class under test
    ReportTooltipInteractor interactor;

    FragmentActivity activity = Robolectric.setupActivity(FragmentActivity.class);

    @Mock
    NavigationHandler navigationHandler;

    @Mock
    BackupProvidersManager backupProvidersManager;

    @Mock
    Analytics analytics;

    @Mock
    GenerateInfoTooltipManager generateInfoTooltipManager;

    @Mock
    BackupReminderTooltipManager backupReminderTooltipManager;

    @Mock
    ImportInfoTooltipManager importInfoTooltipManager;


    private final SyncErrorType errorType = SyncErrorType.NoRemoteDiskSpace;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        interactor = new ReportTooltipInteractor(activity, navigationHandler, backupProvidersManager,
                analytics, generateInfoTooltipManager, backupReminderTooltipManager,
                importInfoTooltipManager, Schedulers.trampoline());

        when(backupProvidersManager.getSyncProvider()).thenReturn(SyncProvider.GoogleDrive);

        when(backupProvidersManager.getCriticalSyncErrorStream()).thenReturn(Observable.never());
        when(importInfoTooltipManager.needToShowImportInfo()).thenReturn(Observable.just(false));
        when(backupReminderTooltipManager.needToShowBackupReminder()).thenReturn(Maybe.empty());
        when(generateInfoTooltipManager.needToShowGenerateTooltip()).thenReturn(Single.just(false));

    }

    @Test
    public void getErrors() throws InterruptedException {
        when(backupProvidersManager.getCriticalSyncErrorStream()).thenReturn(Observable.just(new CriticalSyncError(new Throwable(), errorType)));

        interactor.checkTooltipCauses()
                .test()
                .assertNoErrors()
                .assertValue(ReportTooltipUiIndicator.syncError(errorType));
    }

    @Test
    public void getErrorsFirst() {
        when(backupProvidersManager.getCriticalSyncErrorStream()).thenReturn(Observable.just(new CriticalSyncError(new Throwable(), errorType)));
        when(generateInfoTooltipManager.needToShowGenerateTooltip()).thenReturn(Single.just(true));
        when(backupReminderTooltipManager.needToShowBackupReminder()).thenReturn(Maybe.just(15));
        when(importInfoTooltipManager.needToShowImportInfo()).thenReturn(Observable.just(true));

        interactor.checkTooltipCauses()
                .test()
                .assertNoErrors()
                .assertValue(ReportTooltipUiIndicator.syncError(errorType));
    }

    @Test
    public void getGenerateInfoWhenErrorsArePossible() {
        //Note: with SyncProvider.GoogleDrive if there are no errors getErrorStream() returns Observable.never()
        when(backupProvidersManager.getCriticalSyncErrorStream()).thenReturn(Observable.never());
        when(generateInfoTooltipManager.needToShowGenerateTooltip()).thenReturn(Single.just(true));

        interactor.checkTooltipCauses()
                .test()
                .assertNoErrors()
                .assertValue(ReportTooltipUiIndicator.generateInfo());
    }

    @Test
    public void getGenerateInfoWhenErrorsAreImpossible() {
        //Note: with SyncProvider.None getErrorStream() returns Observable.empty()
        when(backupProvidersManager.getCriticalSyncErrorStream()).thenReturn(Observable.empty());
        when(generateInfoTooltipManager.needToShowGenerateTooltip()).thenReturn(Single.just(true));

        interactor.checkTooltipCauses()
                .test()
                .assertNoErrors()
                .assertValue(ReportTooltipUiIndicator.generateInfo());
    }

    @Test
    public void getNoneWhenErrorsArePossible() {
        when(backupProvidersManager.getCriticalSyncErrorStream()).thenReturn(Observable.never());

        interactor.checkTooltipCauses()
                .test()
                .assertNoErrors()
                .assertValue(ReportTooltipUiIndicator.none());
    }

    @Test
    public void getNoneWhenErrorsAreImpossible() {
        when(backupProvidersManager.getCriticalSyncErrorStream()).thenReturn(Observable.empty());

        interactor.checkTooltipCauses()
                .test()
                .assertNoErrors()
                .assertValue(ReportTooltipUiIndicator.none());
    }

    @Test
    public void getBackupReminder() {
        when(backupReminderTooltipManager.needToShowBackupReminder()).thenReturn(Maybe.just(DAYS));

        interactor.checkTooltipCauses()
                .test()
                .assertNoErrors()
                .assertValue(ReportTooltipUiIndicator.backupReminder(DAYS));
    }

    @Test
    public void getBackupReminderFirst() {
        when(generateInfoTooltipManager.needToShowGenerateTooltip()).thenReturn(Single.just(true));
        when(backupReminderTooltipManager.needToShowBackupReminder()).thenReturn(Maybe.just(DAYS));

        interactor.checkTooltipCauses()
                .test()
                .assertNoErrors()
                .assertValue(ReportTooltipUiIndicator.backupReminder(DAYS));
    }

    @Test
    public void getImportInfo() {
        when(importInfoTooltipManager.needToShowImportInfo()).thenReturn(Observable.just(true));

        interactor.checkTooltipCauses()
                .test()
                .assertNoErrors()
                .assertValue(ReportTooltipUiIndicator.importInfo());
    }

    @Test
    public void getImportInfoFirst() {
        when(generateInfoTooltipManager.needToShowGenerateTooltip()).thenReturn(Single.just(true));
        when(backupReminderTooltipManager.needToShowBackupReminder()).thenReturn(Maybe.just(DAYS));
        when(importInfoTooltipManager.needToShowImportInfo()).thenReturn(Observable.just(true));

        interactor.checkTooltipCauses()
                .test()
                .assertNoErrors()
                .assertValue(ReportTooltipUiIndicator.importInfo());
    }
}

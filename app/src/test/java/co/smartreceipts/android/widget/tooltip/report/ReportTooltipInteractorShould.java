package co.smartreceipts.android.widget.tooltip.report;

import android.support.v4.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.errors.CriticalSyncError;
import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.widget.tooltip.report.generate.GenerateInfoTooltipManager;
import io.reactivex.Observable;
import io.reactivex.Single;

import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportTooltipInteractorShould {

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

    private final SyncErrorType errorType = SyncErrorType.NoRemoteDiskSpace;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        interactor = new ReportTooltipInteractor(activity, navigationHandler, backupProvidersManager, analytics, generateInfoTooltipManager);

        when(backupProvidersManager.getSyncProvider()).thenReturn(SyncProvider.GoogleDrive);

    }

    @Test
    public void getErrors() throws InterruptedException {
        when(backupProvidersManager.getCriticalSyncErrorStream()).thenReturn(Observable.just(new CriticalSyncError(new Throwable(), errorType)));
        when(generateInfoTooltipManager.needToShowGenerateTooltip()).thenReturn(Single.just(false));

        interactor.checkTooltipCauses()
                .test()
                .assertNoErrors()
                .assertValue(ReportTooltipUiIndicator.syncError(errorType));
    }

    @Test
    public void getErrorsFirst() {
        when(backupProvidersManager.getCriticalSyncErrorStream()).thenReturn(Observable.just(new CriticalSyncError(new Throwable(), errorType)));
        when(generateInfoTooltipManager.needToShowGenerateTooltip()).thenReturn(Single.just(true));

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
        when(generateInfoTooltipManager.needToShowGenerateTooltip()).thenReturn(Single.just(false));

        interactor.checkTooltipCauses()
                .test()
                .assertNoErrors()
                .assertValue(ReportTooltipUiIndicator.none());
    }

    @Test
    public void getNoneWhenErrorsAreImpossible() {
        when(backupProvidersManager.getCriticalSyncErrorStream()).thenReturn(Observable.empty());
        when(generateInfoTooltipManager.needToShowGenerateTooltip()).thenReturn(Single.just(false));

        interactor.checkTooltipCauses()
                .test()
                .assertNoErrors()
                .assertValue(ReportTooltipUiIndicator.none());
    }
}

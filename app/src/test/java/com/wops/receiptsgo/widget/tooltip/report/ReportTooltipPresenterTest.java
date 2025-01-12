package com.wops.receiptsgo.widget.tooltip.report;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import com.wops.analytics.Analytics;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.core.sync.errors.SyncErrorType;
import com.wops.core.sync.provider.SyncProvider;
import com.wops.receiptsgo.widget.tooltip.LegacyTooltipView;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportTooltipPresenterTest {

    private static final int DAYS = 15;

    ReportTooltipPresenter presenter;

    @Mock
    LegacyTooltipView tooltipView;

    @Mock
    ReportTooltipInteractor interactor;

    @Mock
    BackupProvidersManager backupProvidersManager;

    @Mock
    Analytics analytics;

    private final SyncErrorType errorType = SyncErrorType.NoRemoteDiskSpace;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(backupProvidersManager.getSyncProvider()).thenReturn(SyncProvider.GoogleDrive);
        when(tooltipView.getCloseButtonClicks()).thenReturn(Observable.never());
        when(tooltipView.getTooltipsClicks()).thenReturn(Observable.never());

        presenter = new ReportTooltipPresenter(tooltipView, interactor, backupProvidersManager, analytics, Schedulers.trampoline(), Schedulers.trampoline());
    }

    @Test
    public void passErrorTooltipClicks() {
        when(interactor.checkTooltipCauses()).thenReturn(Observable.just(ReportTooltipUiIndicator.syncError(errorType)));
        when(tooltipView.getTooltipsClicks()).thenReturn(Observable.just(ReportTooltipUiIndicator.syncError(errorType)));

        presenter.subscribe();

        verify(tooltipView).present(ReportTooltipUiIndicator.syncError(errorType));
        verify(tooltipView).present(ReportTooltipUiIndicator.none());
        verify(tooltipView, never()).present(ReportTooltipUiIndicator.generateInfo());
        verify(interactor).handleClickOnErrorTooltip(errorType);
    }

    @Test
    public void passGenerateInfoTooltipClicks() {
        when(interactor.checkTooltipCauses()).thenReturn(Observable.just(ReportTooltipUiIndicator.generateInfo()));
        when(tooltipView.getTooltipsClicks()).thenReturn(Observable.just(ReportTooltipUiIndicator.generateInfo()));

        presenter.subscribe();

        verify(tooltipView).present(ReportTooltipUiIndicator.generateInfo());
        verify(tooltipView).present(ReportTooltipUiIndicator.none());
        verify(interactor).generateInfoTooltipClosed();
    }

    @Test
    public void passGenerateInfoTooltipCloseClicks() {
        when(interactor.checkTooltipCauses()).thenReturn(Observable.just(ReportTooltipUiIndicator.generateInfo()));
        when(tooltipView.getCloseButtonClicks()).thenReturn(Observable.just(ReportTooltipUiIndicator.generateInfo()));

        presenter.subscribe();

        verify(tooltipView).present(ReportTooltipUiIndicator.generateInfo());
        verify(tooltipView).present(ReportTooltipUiIndicator.none());
        verify(interactor).generateInfoTooltipClosed();
    }

    @Test
    public void passErrorTooltipCloseClicks() {
        when(interactor.checkTooltipCauses()).thenReturn(Observable.just(ReportTooltipUiIndicator.syncError(errorType)));
        when(tooltipView.getCloseButtonClicks()).thenReturn(Observable.just(ReportTooltipUiIndicator.syncError(errorType)));

        presenter.subscribe();

        verify(tooltipView).present(ReportTooltipUiIndicator.syncError(errorType));
        verify(tooltipView).present(ReportTooltipUiIndicator.none());
        verify(interactor, never()).handleClickOnErrorTooltip(errorType);
    }

    @Test
    public void passBackupTooltipClicks() {
        when(interactor.checkTooltipCauses()).thenReturn(Observable.just(ReportTooltipUiIndicator.backupReminder(DAYS)));
        when(tooltipView.getTooltipsClicks()).thenReturn(Observable.just(ReportTooltipUiIndicator.backupReminder(DAYS)));

        presenter.subscribe();

        verify(tooltipView).present(ReportTooltipUiIndicator.backupReminder(DAYS));
        verify(tooltipView).present(ReportTooltipUiIndicator.none());
        verify(interactor).backupReminderTooltipClosed();
    }

    @Test
    public void passBackupTooltipCloseClicks() {
        when(interactor.checkTooltipCauses()).thenReturn(Observable.just(ReportTooltipUiIndicator.backupReminder(DAYS)));
        when(tooltipView.getCloseButtonClicks()).thenReturn(Observable.just(ReportTooltipUiIndicator.backupReminder(DAYS)));

        presenter.subscribe();

        verify(tooltipView).present(ReportTooltipUiIndicator.backupReminder(DAYS));
        verify(tooltipView).present(ReportTooltipUiIndicator.none());
        verify(interactor).backupReminderTooltipClosed();
    }

    @Test
    public void passImportInfoTooltipCloseClicks() {
        when(interactor.checkTooltipCauses()).thenReturn(Observable.just(ReportTooltipUiIndicator.importInfo()));
        when(tooltipView.getCloseButtonClicks()).thenReturn(Observable.just(ReportTooltipUiIndicator.importInfo()));

        presenter.subscribe();

        verify(tooltipView).present(ReportTooltipUiIndicator.importInfo());
        verify(tooltipView).present(ReportTooltipUiIndicator.none());
        verify(interactor).importInfoTooltipClosed();
    }
}

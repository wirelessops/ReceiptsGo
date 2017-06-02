package co.smartreceipts.android.widget.tooltip.report;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.widget.tooltip.TooltipView;
import io.reactivex.Observable;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportTooltipPresenterShould {

    // Class under test
    ReportTooltipPresenter presenter;

    @Mock
    TooltipView tooltipView;

    @Mock
    ReportTooltipInteractor interactor;

    @Mock
    BackupProvidersManager backupProvidersManager;


    private final SyncErrorType errorType = SyncErrorType.NoRemoteDiskSpace;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        presenter = new ReportTooltipPresenter(tooltipView, interactor, backupProvidersManager);

        when(backupProvidersManager.getSyncProvider()).thenReturn(SyncProvider.GoogleDrive);
        when(tooltipView.getCloseButtonClicks()).thenReturn(Observable.never());
        when(tooltipView.getTooltipsClicks()).thenReturn(Observable.never());

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
}

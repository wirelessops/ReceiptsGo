package co.smartreceipts.android.ocr.widget.tooltip;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OcrInformationalTooltipInteractorTest {

    // Class under test
    OcrInformationalTooltipInteractor interactor;

    @Mock
    NavigationHandler navigationHandler;

    @Mock
    Analytics analytics;

    @Mock
    OcrInformationalTooltipStateTracker stateTracker;

    @Mock
    OcrPurchaseTracker ocrPurchaseTracker;

    @Mock
    IdentityManager identityManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        interactor = new OcrInformationalTooltipInteractor(navigationHandler, analytics, stateTracker, ocrPurchaseTracker, identityManager);
    }

    @Test
    public void getShowOcrTooltipForLotsOfPurchasesWhenSetToShowTooltip() {
        when(stateTracker.shouldShowOcrInfo()).thenReturn(Single.just(true));
        when(ocrPurchaseTracker.getRemainingScans()).thenReturn(6);
        when(ocrPurchaseTracker.hasAvailableScans()).thenReturn(true);
        when(identityManager.isLoggedIn()).thenReturn(true);

        TestObserver<OcrTooltipMessageType> testObserver = interactor.getShowOcrTooltip().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(stateTracker, never()).setShouldShowOcrInfo(true);
    }

    @Test
    public void getShowOcrTooltipForLimitedPurchasesWhenSetToShowTooltip() {
        when(stateTracker.shouldShowOcrInfo()).thenReturn(Single.just(true));
        when(ocrPurchaseTracker.getRemainingScans()).thenReturn(5);
        when(identityManager.isLoggedIn()).thenReturn(true);

        TestObserver<OcrTooltipMessageType> testObserver = interactor.getShowOcrTooltip().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertValue(OcrTooltipMessageType.LimitedScansRemaining);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(stateTracker).setShouldShowOcrInfo(true);
    }

    @Test
    public void getShowOcrTooltipForLimitedPurchasesWhenNotSetToShowTooltip() {
        when(stateTracker.shouldShowOcrInfo()).thenReturn(Single.just(false));
        when(ocrPurchaseTracker.getRemainingScans()).thenReturn(5);
        when(identityManager.isLoggedIn()).thenReturn(true);

        TestObserver<OcrTooltipMessageType> testObserver = interactor.getShowOcrTooltip().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertValue(OcrTooltipMessageType.LimitedScansRemaining);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(stateTracker).setShouldShowOcrInfo(true);
    }

    @Test
    public void getShowOcrTooltipForNotConfiguredWhenSetToShowTooltip() {
        when(stateTracker.shouldShowOcrInfo()).thenReturn(Single.just(true));

        TestObserver<OcrTooltipMessageType> testObserver = interactor.getShowOcrTooltip().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertValue(OcrTooltipMessageType.NotConfigured);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(stateTracker, never()).setShouldShowOcrInfo(true);
    }

    @Test
    public void getShowOcrTooltipForNotConfiguredWhenNotSetToShowTooltip() {
        when(stateTracker.shouldShowOcrInfo()).thenReturn(Single.just(false));

        TestObserver<OcrTooltipMessageType> testObserver = interactor.getShowOcrTooltip().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(stateTracker, never()).setShouldShowOcrInfo(true);
    }

    @Test
    public void getShowOcrTooltipForNoRemainingPurchasesWhenSetToShowTooltip() {
        when(stateTracker.shouldShowOcrInfo()).thenReturn(Single.just(true));
        when(ocrPurchaseTracker.hasAvailableScans()).thenReturn(false);
        when(identityManager.isLoggedIn()).thenReturn(true);

        TestObserver<OcrTooltipMessageType> testObserver = interactor.getShowOcrTooltip().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertValue(OcrTooltipMessageType.NoScansRemaining);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(stateTracker, never()).setShouldShowOcrInfo(true);
    }

    @Test
    public void getShowOcrTooltipForNoRemainingPurchasesWhenNotSetToShowTooltip() {
        when(stateTracker.shouldShowOcrInfo()).thenReturn(Single.just(false));
        when(ocrPurchaseTracker.hasAvailableScans()).thenReturn(false);
        when(identityManager.isLoggedIn()).thenReturn(true);

        TestObserver<OcrTooltipMessageType> testObserver = interactor.getShowOcrTooltip().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(stateTracker, never()).setShouldShowOcrInfo(true);
    }

    @Test
    public void dismissTooltip() {
        interactor.dismissTooltip();
        verify(stateTracker).setShouldShowOcrInfo(false);
        verifyZeroInteractions(navigationHandler);
    }

    @Test
    public void showOcrInformation() {
        interactor.showOcrConfiguration();
        verify(stateTracker).setShouldShowOcrInfo(false);
        verify(navigationHandler).navigateToOcrConfigurationFragment();
    }
}
package co.smartreceipts.android.ocr.widget.tooltip;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker;
import co.smartreceipts.android.utils.ConfigurableResourceFeature;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OcrInformationalTooltipInteractorTest {

    // Class under test
    OcrInformationalTooltipInteractor interactor;

    @Mock
    Analytics analytics;

    @Mock
    OcrInformationalTooltipStateTracker stateTracker;

    @Mock
    OcrPurchaseTracker ocrPurchaseTracker;

    @Mock
    IdentityManager identityManager;

    @Mock
    ConfigurationManager configurationManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(configurationManager.isEnabled(ConfigurableResourceFeature.Ocr)).thenReturn(true);
        interactor = new OcrInformationalTooltipInteractor(analytics, stateTracker, ocrPurchaseTracker, identityManager, configurationManager, Schedulers.trampoline());
    }

    @Test
    public void initializeWithNoScansRemaining() {
        when(ocrPurchaseTracker.getRemainingScansStream()).thenReturn(Observable.just(0));
        interactor.initialize();
        verifyZeroInteractions(stateTracker);
    }

    @Test
    public void initializeWithManyScansRemaining() {
        when(ocrPurchaseTracker.getRemainingScansStream()).thenReturn(Observable.just(100));
        interactor.initialize();
        verifyZeroInteractions(stateTracker);
    }

    @Test
    public void initializeWithMinimumScansRemainingToProvideHint() {
        when(ocrPurchaseTracker.getRemainingScansStream()).thenReturn(Observable.just(OcrInformationalTooltipInteractor.SCANS_LEFT_TO_INFORM));
        interactor.initialize();
        verify(stateTracker).setShouldShowOcrInfo(true);
    }

    @Test
    public void initializeWithOneThenZeroScansRemaining() {
        when(ocrPurchaseTracker.getRemainingScansStream()).thenReturn(Observable.just(1, 0));
        interactor.initialize();
        verify(stateTracker).setShouldShowOcrInfo(true);
    }

    @Test
    public void getShowOcrTooltipWhenOcrIsNotEnabled() {
        when(stateTracker.shouldShowOcrInfo()).thenReturn(Single.just(true));
        when(configurationManager.isEnabled(ConfigurableResourceFeature.Ocr)).thenReturn(false);

        TestObserver<OcrTooltipMessageType> testObserver = interactor.getShowOcrTooltip().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(stateTracker, never()).setShouldShowOcrInfo(anyBoolean());
    }

    @Test
    public void getShowOcrTooltipForLotsOfPurchasesWhenSetToShowTooltip() {
        when(stateTracker.shouldShowOcrInfo()).thenReturn(Single.just(true));
        when(ocrPurchaseTracker.getRemainingScans()).thenReturn(OcrInformationalTooltipInteractor.SCANS_LEFT_TO_INFORM + 1);
        when(ocrPurchaseTracker.hasAvailableScans()).thenReturn(true);
        when(identityManager.isLoggedIn()).thenReturn(true);

        TestObserver<OcrTooltipMessageType> testObserver = interactor.getShowOcrTooltip().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(stateTracker, never()).setShouldShowOcrInfo(anyBoolean());
    }

    @Test
    public void getShowOcrTooltipForLimitedPurchasesWhenSetToShowTooltip() {
        when(stateTracker.shouldShowOcrInfo()).thenReturn(Single.just(true));
        when(ocrPurchaseTracker.getRemainingScans()).thenReturn(OcrInformationalTooltipInteractor.SCANS_LEFT_TO_INFORM);
        when(identityManager.isLoggedIn()).thenReturn(true);

        TestObserver<OcrTooltipMessageType> testObserver = interactor.getShowOcrTooltip().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertValue(OcrTooltipMessageType.LimitedScansRemaining);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(stateTracker, never()).setShouldShowOcrInfo(anyBoolean());
    }

    @Test
    public void getShowOcrTooltipForLimitedPurchasesWhenNotSetToShowTooltip() {
        when(stateTracker.shouldShowOcrInfo()).thenReturn(Single.just(false));
        when(ocrPurchaseTracker.getRemainingScans()).thenReturn(OcrInformationalTooltipInteractor.SCANS_LEFT_TO_INFORM);
        when(identityManager.isLoggedIn()).thenReturn(true);

        TestObserver<OcrTooltipMessageType> testObserver = interactor.getShowOcrTooltip().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(stateTracker, never()).setShouldShowOcrInfo(anyBoolean());
    }

    @Test
    public void getShowOcrTooltipForNotConfiguredWhenSetToShowTooltip() {
        when(stateTracker.shouldShowOcrInfo()).thenReturn(Single.just(true));

        TestObserver<OcrTooltipMessageType> testObserver = interactor.getShowOcrTooltip().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertValue(OcrTooltipMessageType.NotConfigured);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(stateTracker, never()).setShouldShowOcrInfo(anyBoolean());
    }

    @Test
    public void getShowOcrTooltipForNotConfiguredWhenNotSetToShowTooltip() {
        when(stateTracker.shouldShowOcrInfo()).thenReturn(Single.just(false));

        TestObserver<OcrTooltipMessageType> testObserver = interactor.getShowOcrTooltip().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(stateTracker, never()).setShouldShowOcrInfo(anyBoolean());
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
        verify(stateTracker, never()).setShouldShowOcrInfo(anyBoolean());
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
        verify(stateTracker, never()).setShouldShowOcrInfo(anyBoolean());
    }

    @Test
    public void dismissTooltip() {
        interactor.markTooltipDismissed();
        verify(stateTracker).setShouldShowOcrInfo(false);
    }

    @Test
    public void showOcrInformation() {
        interactor.markTooltipShown();
        verify(stateTracker).setShouldShowOcrInfo(false);
    }
}
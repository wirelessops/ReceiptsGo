package co.smartreceipts.android.ocr.widget.configuration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.model.AvailablePurchase;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.core.identity.IdentityManager;
import co.smartreceipts.core.identity.store.EmailAddress;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OcrConfigurationInteractorTest {

    @InjectMocks
    OcrConfigurationInteractor interactor;

    @Mock
    IdentityManager identityManager;

    @Mock
    OcrPurchaseTracker ocrPurchaseTracker;

    @Mock
    PurchaseManager purchaseManager;

    @Mock
    UserPreferenceManager userPreferenceManager;

    @Mock
    Analytics analytics;

    @Mock
    AvailablePurchase availablePurchase, availablePurchase2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getEmail() {
        final EmailAddress emailAddress = new EmailAddress("email");
        when(identityManager.getEmail()).thenReturn(emailAddress);
        assertEquals(emailAddress, interactor.getEmail());
    }

    @Test
    public void getRemainingScansStream() {
        final PublishSubject<Integer> scanSubject = PublishSubject.create();
        when(ocrPurchaseTracker.getRemainingScansStream()).thenReturn(scanSubject);

        TestObserver<Integer> testObserver = interactor.getRemainingScansStream().test();
        scanSubject.onNext(61);

        testObserver.assertValue(61);
        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void getAvailableOcrPurchasesOrdersByPrice() {
        when(availablePurchase.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans50);
        when(availablePurchase2.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans10);
        when(availablePurchase.getPriceAmountMicros()).thenReturn(500000L);
        when(availablePurchase2.getPriceAmountMicros()).thenReturn(100000L);

        final Set<AvailablePurchase> purchaseSet = new HashSet<>(Arrays.asList(availablePurchase, availablePurchase2));
        when(purchaseManager.getAllAvailablePurchases()).thenReturn(Observable.just(purchaseSet));

        TestObserver<List<AvailablePurchase>> testObserver = interactor.getAvailableOcrPurchases().test();

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(Arrays.asList(availablePurchase2, availablePurchase));
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void getAvailableOcrPurchasesIgnoresSubscriptions() {
        when(availablePurchase.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans50);
        when(availablePurchase2.getInAppPurchase()).thenReturn(InAppPurchase.SmartReceiptsPlus);
        when(availablePurchase.getPriceAmountMicros()).thenReturn(500000L);
        when(availablePurchase2.getPriceAmountMicros()).thenReturn(100000L);

        final Set<AvailablePurchase> purchaseSet = new HashSet<>(Arrays.asList(availablePurchase, availablePurchase2));
        when(purchaseManager.getAllAvailablePurchases()).thenReturn(Observable.just(purchaseSet));

        TestObserver<List<AvailablePurchase>> testObserver = interactor.getAvailableOcrPurchases().test();

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(Collections.singletonList(availablePurchase));
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void getAvailableOcrPurchasesIgnoresNonOcrOnes() {
        when(availablePurchase.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans50);
        when(availablePurchase2.getInAppPurchase()).thenReturn(InAppPurchase.TestConsumablePurchase);
        when(availablePurchase.getPriceAmountMicros()).thenReturn(500000L);
        when(availablePurchase2.getPriceAmountMicros()).thenReturn(100000L);

        final Set<AvailablePurchase> purchaseSet = new HashSet<>(Arrays.asList(availablePurchase, availablePurchase2));
        when(purchaseManager.getAllAvailablePurchases()).thenReturn(Observable.just(purchaseSet));

        TestObserver<List<AvailablePurchase>> testObserver = interactor.getAvailableOcrPurchases().test();

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(Collections.singletonList(availablePurchase));
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void startOcrPurchase() {
        when(availablePurchase.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans50);
        interactor.startOcrPurchase(availablePurchase.getInAppPurchase());
        verify(purchaseManager).initiatePurchase(InAppPurchase.OcrScans50, PurchaseSource.Ocr);
    }

    @Test
    public void getOcrIsEnabled() {
        when(userPreferenceManager.getObservable(UserPreference.Misc.OcrIsEnabled)).thenReturn(Observable.just(false));
        final TestObserver<Boolean> testObserver1 = interactor.getOcrIsEnabled().test();
        testObserver1.awaitTerminalEvent();
        testObserver1.assertValue(false);
        testObserver1.assertComplete();
        testObserver1.assertNoErrors();

        when(userPreferenceManager.getObservable(UserPreference.Misc.OcrIsEnabled)).thenReturn(Observable.just(true));
        final TestObserver<Boolean> testObserver2 = interactor.getOcrIsEnabled().test();
        testObserver2.awaitTerminalEvent();
        testObserver2.assertValue(true);
        testObserver2.assertComplete();
        testObserver2.assertNoErrors();
    }

    @Test
    public void setOcrIsEnabled() {
        interactor.setOcrIsEnabled(false);
        verify(userPreferenceManager).set(UserPreference.Misc.OcrIsEnabled, false);

        interactor.setOcrIsEnabled(true);
        verify(userPreferenceManager).set(UserPreference.Misc.OcrIsEnabled, true);
    }

    @Test
    public void getAllowUsToSaveImagesRemotely() {
        when(userPreferenceManager.getObservable(UserPreference.Misc.OcrIncognitoMode)).thenReturn(Observable.just(true));
        final TestObserver<Boolean> testObserver1 = interactor.getAllowUsToSaveImagesRemotely().test();
        testObserver1.awaitTerminalEvent();
        testObserver1.assertValue(false);
        testObserver1.assertComplete();
        testObserver1.assertNoErrors();

        when(userPreferenceManager.getObservable(UserPreference.Misc.OcrIncognitoMode)).thenReturn(Observable.just(false));
        final TestObserver<Boolean> testObserver2 = interactor.getAllowUsToSaveImagesRemotely().test();
        testObserver2.awaitTerminalEvent();
        testObserver2.assertValue(true);
        testObserver2.assertComplete();
        testObserver2.assertNoErrors();
    }

    @Test
    public void setAllowUsToSaveImagesRemotely() {
        interactor.setAllowUsToSaveImagesRemotely(false);
        verify(userPreferenceManager).set(UserPreference.Misc.OcrIncognitoMode, true);

        interactor.setAllowUsToSaveImagesRemotely(true);
        verify(userPreferenceManager).set(UserPreference.Misc.OcrIncognitoMode, false);
    }

}
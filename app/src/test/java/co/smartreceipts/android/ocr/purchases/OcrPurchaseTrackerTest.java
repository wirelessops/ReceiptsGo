package co.smartreceipts.android.ocr.purchases;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;
import java.util.Set;

import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.apis.me.MeResponse;
import co.smartreceipts.android.identity.apis.me.User;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.apis.MobileAppPurchasesService;
import co.smartreceipts.android.purchases.apis.PurchaseRequest;
import co.smartreceipts.android.purchases.apis.PurchaseResponse;
import co.smartreceipts.android.purchases.model.ConsumablePurchase;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OcrPurchaseTrackerTest {

    private static final int REMAINING_SCANS = 49;

    // Class under test
    OcrPurchaseTracker ocrPurchaseTracker;

    @Mock
    IdentityManager identityManager;

    @Mock
    ServiceManager serviceManager;

    @Mock
    PurchaseManager purchaseManager;

    @Mock
    PurchaseWallet purchaseWallet;

    @Mock
    LocalOcrScansTracker localOcrScansTracker;

    @Mock
    ConsumablePurchase consumablePurchase;

    @Mock
    MobileAppPurchasesService mobileAppPurchasesService;

    @Mock
    PurchaseResponse purchaseResponse;

    @Mock
    MeResponse meResponse;

    @Mock
    User user;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(consumablePurchase.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans50);
        when(consumablePurchase.getInAppDataSignature()).thenReturn("");
        when(consumablePurchase.getPurchaseData()).thenReturn("");
        when(purchaseWallet.getManagedProduct(InAppPurchase.OcrScans50)).thenReturn(consumablePurchase);
        when(serviceManager.getService(MobileAppPurchasesService.class)).thenReturn(mobileAppPurchasesService);
        when(identityManager.isLoggedInStream()).thenReturn(Observable.just(true));
        when(identityManager.getMe()).thenReturn(Observable.just(meResponse));
        when(meResponse.getUser()).thenReturn(user);
        when(user.getRecognitionsAvailable()).thenReturn(REMAINING_SCANS);
        ocrPurchaseTracker = new OcrPurchaseTracker(identityManager, serviceManager, purchaseManager, purchaseWallet, localOcrScansTracker, Schedulers.trampoline());
    }

    @Test
    public void initializeWhenNotLoggedInDoesNothing() {
        // Configure
        when(identityManager.isLoggedInStream()).thenReturn(Observable.just(false));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker, never()).setRemainingScans(REMAINING_SCANS);
        verifyNoMoreInteractions(purchaseManager);
        verifyZeroInteractions(serviceManager);
    }

    @Test
    public void initializeThrowsException() {
        // Configure
        when(purchaseManager.getAllOwnedPurchases()).thenReturn(Observable.<Set<ManagedProduct>>error(new Exception("test")));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(purchaseManager, never()).consumePurchase(any(ConsumablePurchase.class));
        verifyZeroInteractions(serviceManager);
    }

    @Test
    public void initializeUploadFails() {
        // Configure
        when(purchaseManager.getAllOwnedPurchases()).thenReturn(Observable.just(Collections.singleton((ManagedProduct) consumablePurchase)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.<PurchaseResponse>error(new Exception("test")));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(purchaseManager, never()).consumePurchase(any(ConsumablePurchase.class));
    }

    @Test
    public void initializeSucceeds() {
        // Configure
        when(purchaseManager.getAllOwnedPurchases()).thenReturn(Observable.just(Collections.singleton((ManagedProduct) consumablePurchase)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(purchaseManager).consumePurchase(consumablePurchase);
    }

    @Test
    public void initializeFailsToFetchMe() {
        // Configure
        when(identityManager.getMe()).thenReturn(Observable.<MeResponse>error(new Exception("test")));
        when(purchaseManager.getAllOwnedPurchases()).thenReturn(Observable.just(Collections.singleton((ManagedProduct) consumablePurchase)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker, never()).setRemainingScans(anyInt());
        verify(purchaseManager).consumePurchase(consumablePurchase);
    }

    @Test
    public void initializeReturnsInvalidMeResponse() {
        // Configure
        when(meResponse.getUser()).thenReturn(null);
        when(purchaseManager.getAllOwnedPurchases()).thenReturn(Observable.just(Collections.singleton((ManagedProduct) consumablePurchase)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker, never()).setRemainingScans(anyInt());
        verify(purchaseManager).consumePurchase(consumablePurchase);
    }

    @Test
    public void initializeSucceedsForLateLogin() {
        // Configure
        final PublishSubject<Boolean> loggedInStream = PublishSubject.create();
        loggedInStream.onNext(false);
        when(identityManager.isLoggedInStream()).thenReturn(loggedInStream);
        when(purchaseManager.getAllOwnedPurchases()).thenReturn(Observable.just(Collections.singleton((ManagedProduct) consumablePurchase)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.initialize();
        loggedInStream.onNext(true);

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(purchaseManager).consumePurchase(consumablePurchase);
    }

    @Test
    public void onPurchaseSuccessWhenNotLoggedIn() {
        // Configure
        when(identityManager.isLoggedInStream()).thenReturn(Observable.just(false));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);

        // Verify
        verify(purchaseManager, never()).consumePurchase(any(ConsumablePurchase.class));
        verify(localOcrScansTracker, never()).setRemainingScans(REMAINING_SCANS);
        verifyZeroInteractions(serviceManager);
    }

    @Test
    public void onPurchaseSuccessForUnTrackedType() {
        // Configure

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.TestConsumablePurchase, PurchaseSource.Unknown);

        // Verify
        verify(purchaseManager, never()).consumePurchase(any(ConsumablePurchase.class));
        verify(localOcrScansTracker, never()).setRemainingScans(REMAINING_SCANS);
        verifyZeroInteractions(serviceManager);
    }

    @Test
    public void onPurchaseSuccessUploadFails() {
        // Configure
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.<PurchaseResponse>error(new Exception("test")));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);

        // Verify
        verify(purchaseManager, never()).consumePurchase(any(ConsumablePurchase.class));
        verify(localOcrScansTracker, never()).setRemainingScans(REMAINING_SCANS);
    }

    @Test
    public void onPurchaseSuccessSucceedsButConsumeFails() {
        // Configure
        when(purchaseManager.consumePurchase(consumablePurchase)).thenReturn(Completable.error(new Exception("test")));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);

        // Verify
        verify(purchaseManager).consumePurchase(consumablePurchase);
        verify(localOcrScansTracker, never()).setRemainingScans(REMAINING_SCANS);
    }

    @Test
    public void onPurchaseSuccessSucceeds() {
        // Configure
        when(purchaseManager.consumePurchase(consumablePurchase)).thenReturn(Completable.complete());
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);

        // Verify
        verify(purchaseManager).consumePurchase(consumablePurchase);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
    }

    @Test
    public void onPurchaseSuccessSucceedsForOtherPurchaseType() {
        // Configure
        when(consumablePurchase.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans10);
        when(purchaseWallet.getManagedProduct(InAppPurchase.OcrScans10)).thenReturn(consumablePurchase);
        when(purchaseManager.consumePurchase(consumablePurchase)).thenReturn(Completable.complete());
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans10, PurchaseSource.Unknown);

        // Verify
        verify(purchaseManager).consumePurchase(consumablePurchase);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
    }

    @Test
    public void onPurchaseSuccessSucceedsButFailsToFetchMe() {
        // Configure
        when(identityManager.getMe()).thenReturn(Observable.<MeResponse>error(new Exception("test")));
        when(purchaseManager.consumePurchase(consumablePurchase)).thenReturn(Completable.complete());
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);

        // Verify
        verify(purchaseManager).consumePurchase(consumablePurchase);
        verify(localOcrScansTracker, never()).setRemainingScans(anyInt());
    }

    @Test
    public void onPurchaseSuccessSucceedsButReturnsInvalidMeResponse() {
        // Configure
        when(meResponse.getUser()).thenReturn(null);
        when(purchaseManager.consumePurchase(consumablePurchase)).thenReturn(Completable.complete());
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);

        // Verify
        verify(purchaseManager).consumePurchase(consumablePurchase);
        verify(localOcrScansTracker, never()).setRemainingScans(anyInt());
    }

    @Test
    public void onPurchaseSuccessSucceedsForLateLogin() {
        // Configure
        final PublishSubject<Boolean> loggedInStream = PublishSubject.create();
        loggedInStream.onNext(false);
        when(identityManager.isLoggedInStream()).thenReturn(loggedInStream);
        when(purchaseManager.consumePurchase(consumablePurchase)).thenReturn(Completable.complete());
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);
        loggedInStream.onNext(true);

        // Verify
        verify(purchaseManager).consumePurchase(consumablePurchase);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
    }

    @Test
    public void onPurchaseFailed() {
        ocrPurchaseTracker.onPurchaseFailed(PurchaseSource.Unknown);
        verifyZeroInteractions(serviceManager, purchaseManager, purchaseWallet, localOcrScansTracker);
    }

    @Test
    public void getRemainingScans() {
        when(localOcrScansTracker.getRemainingScans()).thenReturn(50);
        assertEquals(50, ocrPurchaseTracker.getRemainingScans());
    }

    @Test
    public void getRemainingScansStream() {
        final BehaviorSubject<Integer> scansStream = BehaviorSubject.createDefault(50);
        when(localOcrScansTracker.getRemainingScansStream()).thenReturn(scansStream);

        ocrPurchaseTracker.getRemainingScansStream().test()
                .assertValue(50)
                .assertNotComplete()
                .assertNoErrors();
    }

    @Test
    public void hasAvailableScans() {
        when(localOcrScansTracker.getRemainingScans()).thenReturn(50);
        assertTrue(ocrPurchaseTracker.hasAvailableScans());

        when(localOcrScansTracker.getRemainingScans()).thenReturn(0);
        assertFalse(ocrPurchaseTracker.hasAvailableScans());
    }

    @Test
    public void decrementRemainingScans() {
        ocrPurchaseTracker.decrementRemainingScans();
        verify(localOcrScansTracker).decrementRemainingScans();
    }
}

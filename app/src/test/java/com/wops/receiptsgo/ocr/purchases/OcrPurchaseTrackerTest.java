package com.wops.receiptsgo.ocr.purchases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collections;

import com.wops.receiptsgo.apis.SmartReceiptsApiErrorResponse;
import com.wops.receiptsgo.apis.SmartReceiptsApiException;
import com.wops.receiptsgo.apis.WebServiceManager;
import com.wops.receiptsgo.purchases.PurchaseManager;
import com.wops.receiptsgo.purchases.apis.purchases.MobileAppPurchasesService;
import com.wops.receiptsgo.purchases.apis.purchases.PurchaseRequest;
import com.wops.receiptsgo.purchases.apis.purchases.PurchaseResponse;
import com.wops.receiptsgo.purchases.consumption.DefaultInAppPurchaseConsumer;
import com.wops.receiptsgo.purchases.model.InAppPurchase;
import com.wops.receiptsgo.purchases.model.ManagedProduct;
import com.wops.receiptsgo.purchases.model.PurchaseFamily;
import com.wops.receiptsgo.purchases.source.PurchaseSource;
import com.wops.receiptsgo.purchases.wallet.PurchaseWallet;
import com.wops.core.identity.IdentityManager;
import com.wops.core.identity.apis.me.MeResponse;
import com.wops.core.identity.apis.me.User;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import okhttp3.ResponseBody;
import retrofit2.Response;

@RunWith(RobolectricTestRunner.class)
public class OcrPurchaseTrackerTest {

    private static final int REMAINING_SCANS = 49;

    // Class under test
    OcrPurchaseTracker ocrPurchaseTracker;

    @Mock
    IdentityManager identityManager;

    @Mock
    WebServiceManager webServiceManager;

    @Mock
    PurchaseManager purchaseManager;

    @Mock
    PurchaseWallet purchaseWallet;

    @Mock
    DefaultInAppPurchaseConsumer defaultInAppPurchaseConsumer;

    @Mock
    LocalOcrScansTracker localOcrScansTracker;

    @Mock
    ManagedProduct managedProduct;

    @Mock
    MobileAppPurchasesService mobileAppPurchasesService;

    @Mock
    PurchaseResponse purchaseResponse;

    @Mock
    MeResponse meResponse;

    @Mock
    User user;

    @Mock
    ResponseBody retrofitResponseBody;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(managedProduct.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans50);
        when(managedProduct.getInAppDataSignature()).thenReturn("");
        when(managedProduct.getPurchaseDataJson()).thenReturn("");
        when(defaultInAppPurchaseConsumer.isConsumed(managedProduct, PurchaseFamily.Ocr)).thenReturn(false);
        when(purchaseWallet.getLocalInAppManagedProduct(InAppPurchase.OcrScans50)).thenReturn(managedProduct);
        when(webServiceManager.getService(MobileAppPurchasesService.class)).thenReturn(mobileAppPurchasesService);
        when(identityManager.isLoggedInStream()).thenReturn(Observable.just(true));
        when(identityManager.getMe()).thenReturn(Observable.just(meResponse));
        when(meResponse.getUser()).thenReturn(user);
        when(user.getRecognitionsAvailable()).thenReturn(REMAINING_SCANS);
        ocrPurchaseTracker = new OcrPurchaseTracker(identityManager, webServiceManager, purchaseManager, purchaseWallet, defaultInAppPurchaseConsumer, localOcrScansTracker, Schedulers.trampoline());
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
        verifyZeroInteractions(webServiceManager);
    }

    @Test
    public void initializeThrowsException() {
        // Configure
        when(purchaseManager.getAllOwnedPurchasesAndSync()).thenReturn(Single.error(new Exception("test")));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(defaultInAppPurchaseConsumer, never()).consumePurchase(any(ManagedProduct.class), any(PurchaseFamily.class));
        verifyZeroInteractions(webServiceManager);
    }

    @Test
    public void initializeWithoutPurchases() {
        // Configure
        when(purchaseManager.getAllOwnedPurchasesAndSync()).thenReturn(Single.just(Collections.emptySet()));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.error(new Exception("test")));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(defaultInAppPurchaseConsumer, never()).consumePurchase(any(ManagedProduct.class), any(PurchaseFamily.class));
        verifyZeroInteractions(webServiceManager);
    }

    @Test
    public void initializeWithAnAlreadyConsumedPurchase() {
        // Configure
        when(defaultInAppPurchaseConsumer.isConsumed(managedProduct, PurchaseFamily.Ocr)).thenReturn(true);
        when(purchaseManager.getAllOwnedPurchasesAndSync()).thenReturn(Single.just(Collections.singleton(managedProduct)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.error(new Exception("test")));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(defaultInAppPurchaseConsumer, never()).consumePurchase(any(ManagedProduct.class), any(PurchaseFamily.class));
        verifyZeroInteractions(webServiceManager);
    }

    @Test
    public void initializeUploadFails() {
        // Configure
        when(purchaseManager.getAllOwnedPurchasesAndSync()).thenReturn(Single.just(Collections.singleton(managedProduct)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.error(new Exception("test")));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(defaultInAppPurchaseConsumer, never()).consumePurchase(any(ManagedProduct.class), any(PurchaseFamily.class));
    }

    @SuppressWarnings("ThrowableInstanceNeverThrown")
    @Test
    public void initializeUploadFailsWith422CodeAndNullSmartReceiptsApiException() {
        // Configure
        final SmartReceiptsApiErrorResponse errorResponse = null;
        final SmartReceiptsApiException smartReceiptsApiException = new SmartReceiptsApiException(Response.error(422, retrofitResponseBody), new Exception("test"), errorResponse);
        when(purchaseManager.getAllOwnedPurchasesAndSync()).thenReturn(Single.just(Collections.singleton(managedProduct)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.error(smartReceiptsApiException));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(defaultInAppPurchaseConsumer, never()).consumePurchase(any(ManagedProduct.class), any(PurchaseFamily.class));
    }

    @SuppressWarnings("ThrowableInstanceNeverThrown")
    @Test
    public void initializeUploadFailsWith422CodeAndSmartReceiptsApiExceptionWithNullList() {
        // Configure
        final SmartReceiptsApiErrorResponse errorResponse = new SmartReceiptsApiErrorResponse(null);
        final SmartReceiptsApiException smartReceiptsApiException = new SmartReceiptsApiException(Response.error(422, retrofitResponseBody), new Exception("test"), errorResponse);
        when(purchaseManager.getAllOwnedPurchasesAndSync()).thenReturn(Single.just(Collections.singleton(managedProduct)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.error(smartReceiptsApiException));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(defaultInAppPurchaseConsumer, never()).consumePurchase(any(ManagedProduct.class), any(PurchaseFamily.class));
    }

    @SuppressWarnings("ThrowableInstanceNeverThrown")
    @Test
    public void initializeUploadFailsWith422CodeAndSmartReceiptsApiExceptionWithListOfUnknownErrors() {
        // Configure
        final SmartReceiptsApiErrorResponse errorResponse = new SmartReceiptsApiErrorResponse(Arrays.asList("error1", "error2", "error3"));
        final SmartReceiptsApiException smartReceiptsApiException = new SmartReceiptsApiException(Response.error(422, retrofitResponseBody), new Exception("test"), errorResponse);
        when(purchaseManager.getAllOwnedPurchasesAndSync()).thenReturn(Single.just(Collections.singleton(managedProduct)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.error(smartReceiptsApiException));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(defaultInAppPurchaseConsumer, never()).consumePurchase(any(ManagedProduct.class), any(PurchaseFamily.class));
    }

    @SuppressWarnings("ThrowableInstanceNeverThrown")
    @Test
    public void initializeUploadFailsWith400CodeAndSmartReceiptsApiExceptionWithDuplicatePurchaseError() {
        // Configure
        final SmartReceiptsApiErrorResponse errorResponse = new SmartReceiptsApiErrorResponse(Arrays.asList("error1", "Purchase has already been taken", "error3"));
        final SmartReceiptsApiException smartReceiptsApiException = new SmartReceiptsApiException(Response.error(400, retrofitResponseBody), new Exception("test"), errorResponse);
        when(purchaseManager.getAllOwnedPurchasesAndSync()).thenReturn(Single.just(Collections.singleton(managedProduct)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.error(smartReceiptsApiException));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(defaultInAppPurchaseConsumer, never()).consumePurchase(any(ManagedProduct.class), any(PurchaseFamily.class));
    }

    @SuppressWarnings("ThrowableInstanceNeverThrown")
    @Test
    public void initializeUploadFailsWith422CodeAndSmartReceiptsApiExceptionWithDuplicatePurchaseErrorButThenConsumesThisPurchaseLocally() {
        // Configure
        final SmartReceiptsApiErrorResponse errorResponse = new SmartReceiptsApiErrorResponse(Arrays.asList("error1", "Purchase has already been taken", "error3"));
        final SmartReceiptsApiException smartReceiptsApiException = new SmartReceiptsApiException(Response.error(422, retrofitResponseBody), new Exception("test"), errorResponse);
        when(purchaseManager.getAllOwnedPurchasesAndSync()).thenReturn(Single.just(Collections.singleton(managedProduct)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.error(smartReceiptsApiException));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(defaultInAppPurchaseConsumer).consumePurchase(managedProduct, PurchaseFamily.Ocr);
    }

    @Test
    public void initializeSucceeds() {
        // Configure
        when(purchaseManager.getAllOwnedPurchasesAndSync()).thenReturn(Single.just(Collections.singleton(managedProduct)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(defaultInAppPurchaseConsumer).consumePurchase(managedProduct, PurchaseFamily.Ocr);
    }

    @Test
    public void initializeFailsToFetchMe() {
        // Configure
        when(identityManager.getMe()).thenReturn(Observable.error(new Exception("test")));
        when(purchaseManager.getAllOwnedPurchasesAndSync()).thenReturn(Single.just(Collections.singleton(managedProduct)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker, never()).setRemainingScans(anyInt());
        verify(defaultInAppPurchaseConsumer).consumePurchase(managedProduct, PurchaseFamily.Ocr);
    }

    @Test
    public void initializeReturnsInvalidMeResponse() {
        // Configure
        when(meResponse.getUser()).thenReturn(null);
        when(purchaseManager.getAllOwnedPurchasesAndSync()).thenReturn(Single.just(Collections.singleton(managedProduct)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker, never()).setRemainingScans(anyInt());
        verify(defaultInAppPurchaseConsumer).consumePurchase(managedProduct, PurchaseFamily.Ocr);
    }

    @Test
    public void initializeSucceedsForLateLogin() {
        // Configure
        final PublishSubject<Boolean> loggedInStream = PublishSubject.create();
        loggedInStream.onNext(false);
        when(identityManager.isLoggedInStream()).thenReturn(loggedInStream);
        when(purchaseManager.getAllOwnedPurchasesAndSync()).thenReturn(Single.just(Collections.singleton(managedProduct)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.initialize();
        loggedInStream.onNext(true);

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
        verify(defaultInAppPurchaseConsumer).consumePurchase(managedProduct, PurchaseFamily.Ocr);
    }

    @Test
    public void onPurchaseSuccessWhenNotLoggedIn() {
        // Configure
        when(identityManager.isLoggedInStream()).thenReturn(Observable.just(false));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);

        // Verify
        verify(defaultInAppPurchaseConsumer, never()).consumePurchase(any(ManagedProduct.class), any(PurchaseFamily.class));
        verify(localOcrScansTracker, never()).setRemainingScans(REMAINING_SCANS);
        verifyZeroInteractions(webServiceManager);
    }

    @Test
    public void onPurchaseSuccessForUnTrackedType() {
        // Configure

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.TestConsumablePurchase, PurchaseSource.Unknown);

        // Verify
        verify(defaultInAppPurchaseConsumer, never()).consumePurchase(any(ManagedProduct.class), any(PurchaseFamily.class));
        verify(localOcrScansTracker, never()).setRemainingScans(REMAINING_SCANS);
        verifyZeroInteractions(webServiceManager);
    }

    @Test
    public void onPurchaseSuccessUploadFails() {
        // Configure
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.error(new Exception("test")));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);

        // Verify
        verify(defaultInAppPurchaseConsumer, never()).consumePurchase(any(ManagedProduct.class), any(PurchaseFamily.class));
        verify(localOcrScansTracker, never()).setRemainingScans(REMAINING_SCANS);
    }

    @Test
    public void onPurchaseSuccessSucceedsButConsumeFails() {
        // Configure
        when(defaultInAppPurchaseConsumer.consumePurchase(managedProduct, PurchaseFamily.Ocr)).thenReturn(Completable.error(new Exception("test")));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);

        // Verify
        verify(defaultInAppPurchaseConsumer).consumePurchase(managedProduct, PurchaseFamily.Ocr);
        verify(localOcrScansTracker, never()).setRemainingScans(REMAINING_SCANS);
    }

    @Test
    public void onPurchaseSuccessSucceeds() {
        // Configure
        when(defaultInAppPurchaseConsumer.consumePurchase(managedProduct, PurchaseFamily.Ocr)).thenReturn(Completable.complete());
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);

        // Verify
        verify(defaultInAppPurchaseConsumer).consumePurchase(managedProduct, PurchaseFamily.Ocr);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
    }

    @Test
    public void onPurchaseSuccessSucceedsForOtherPurchaseType() {
        // Configure
        when(managedProduct.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans10);
        when(purchaseWallet.getLocalInAppManagedProduct(InAppPurchase.OcrScans10)).thenReturn(managedProduct);
        when(defaultInAppPurchaseConsumer.consumePurchase(managedProduct, PurchaseFamily.Ocr)).thenReturn(Completable.complete());
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans10, PurchaseSource.Unknown);

        // Verify
        verify(defaultInAppPurchaseConsumer).consumePurchase(managedProduct, PurchaseFamily.Ocr);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
    }

    @Test
    public void onPurchaseSuccessSucceedsButFailsToFetchMe() {
        // Configure
        when(identityManager.getMe()).thenReturn(Observable.error(new Exception("test")));
        when(defaultInAppPurchaseConsumer.consumePurchase(managedProduct, PurchaseFamily.Ocr)).thenReturn(Completable.complete());
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);

        // Verify
        verify(defaultInAppPurchaseConsumer).consumePurchase(managedProduct, PurchaseFamily.Ocr);
        verify(localOcrScansTracker, never()).setRemainingScans(anyInt());
    }

    @Test
    public void onPurchaseSuccessSucceedsButReturnsInvalidMeResponse() {
        // Configure
        when(meResponse.getUser()).thenReturn(null);
        when(defaultInAppPurchaseConsumer.consumePurchase(managedProduct, PurchaseFamily.Ocr)).thenReturn(Completable.complete());
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);

        // Verify
        verify(defaultInAppPurchaseConsumer).consumePurchase(managedProduct, PurchaseFamily.Ocr);
        verify(localOcrScansTracker, never()).setRemainingScans(anyInt());
    }

    @Test
    public void onPurchaseSuccessSucceedsForLateLogin() {
        // Configure
        final PublishSubject<Boolean> loggedInStream = PublishSubject.create();
        loggedInStream.onNext(false);
        when(identityManager.isLoggedInStream()).thenReturn(loggedInStream);
        when(defaultInAppPurchaseConsumer.consumePurchase(managedProduct, PurchaseFamily.Ocr)).thenReturn(Completable.complete());
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);
        loggedInStream.onNext(true);

        // Verify
        verify(defaultInAppPurchaseConsumer).consumePurchase(managedProduct, PurchaseFamily.Ocr);
        verify(localOcrScansTracker).setRemainingScans(REMAINING_SCANS);
    }

    @Test
    public void onPurchaseFailed() {
        ocrPurchaseTracker.onPurchaseFailed(PurchaseSource.Unknown);
        verifyZeroInteractions(webServiceManager, purchaseManager, purchaseWallet, localOcrScansTracker);
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
        when(purchaseWallet.hasActivePurchase(InAppPurchase.StandardSubscriptionPlan)).thenReturn(false);
        when(purchaseWallet.hasActivePurchase(InAppPurchase.StandardSubscriptionPlan)).thenReturn(false);

        when(localOcrScansTracker.getRemainingScans()).thenReturn(50);
        assertTrue(ocrPurchaseTracker.hasAvailableScans());

        when(localOcrScansTracker.getRemainingScans()).thenReturn(0);
        assertFalse(ocrPurchaseTracker.hasAvailableScans());

        when(purchaseWallet.hasActivePurchase(InAppPurchase.StandardSubscriptionPlan)).thenReturn(true);
        assertTrue(ocrPurchaseTracker.hasAvailableScans());
    }

    @Test
    public void decrementRemainingScans() {
        ocrPurchaseTracker.decrementRemainingScans();
        verify(localOcrScansTracker).decrementRemainingScans();
    }
}

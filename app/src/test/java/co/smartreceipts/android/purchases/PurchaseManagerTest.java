package co.smartreceipts.android.purchases;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.purchases.model.AvailablePurchase;
import co.smartreceipts.android.purchases.model.ConsumablePurchase;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.model.Subscription;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import io.reactivex.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySetOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PurchaseManagerTest {

    // Service response codes
    private static final int RESULT_OK = 0;
    private static final int RESULT_USER_CANCELED = 1;
    private static final int RESULT_BILLING_UNAVAILABLE = 3;
    private static final int RESULT_ITEM_UNAVAILABLE = 4;
    private static final int RESULT_DEVELOPER_ERROR = 5;
    private static final int RESULT_ERROR = 6;
    private static final int RESULT_ITEM_ALREADY_OWNED = 7;
    private static final int RESULT_ITEM_NOT_OWNED = 8;

    // Purchase state codes
    private static final int PURCHASE_STATE_PURCHASED = 0;
    private static final int PURCHASE_STATE_CANCELLED = 1;
    private static final int PURCHASE_STATE_REFUNDED = 2;

    // Metadata
    private static final String PURCHASE_TOKEN = "012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689";
    private static final String IN_APP_DATA_SIGNATURE = "012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689==";

    // Class under test
    PurchaseManager purchaseManager;

    Application application = RuntimeEnvironment.application;

    ShadowApplication shadowApplication = Shadows.shadowOf(application);

    String packageName = RuntimeEnvironment.application.getPackageName();

    @Mock
    PurchaseWallet purchaseWallet;

    @Mock
    Analytics analytics;

    @Mock
    IBinder binder;

    @Mock
    IInAppBillingService inAppBillingService;

    @Mock
    ConsumablePurchase consumablePurchase;

    @Mock
    PurchaseEventsListener listener1, listener2, listener3;

    @Captor
    ArgumentCaptor<Bundle> bundleCaptor;

    @Captor
    ArgumentCaptor<Set<ManagedProduct>> updateManagedProductsCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(binder.queryLocalInterface("com.android.vending.billing.IInAppBillingService")).thenReturn(inAppBillingService);
        shadowApplication.setComponentNameAndServiceForBindService(new ComponentName("com.android.vending.billing", "InAppBillingService"), binder);

        purchaseManager = new PurchaseManager(application, purchaseWallet, analytics, Schedulers.trampoline(), Schedulers.trampoline());
        purchaseManager.addEventListener(listener1);
        purchaseManager.addEventListener(listener2);
        purchaseManager.addEventListener(listener3);
        purchaseManager.removeEventListener(listener3);
    }

    @Test
    public void onCreate() {
        purchaseManager.initialize(application);
        verifyInAppBillingServiceConnected();
        verify(purchaseWallet).getActivePurchases();
    }

    @Test
    public void initializeSubscriptionsThrowsRemoteException() throws Exception {
        // Configure
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenThrow(new RemoteException());
        final Bundle getConsumablePurchasesResponse = new Bundle();
        getConsumablePurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>());
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>());
        getConsumablePurchasesResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>());
        when(inAppBillingService.getPurchases(3, packageName, "inapp", null)).thenReturn(getConsumablePurchasesResponse);

        // Test
        purchaseManager.initialize(application);

        // Verify
        verifyInAppBillingServiceConnected();
        verifyZeroInteractions(purchaseWallet);
    }

    @Test
    public void initializeConsumablePurchasesThrowsRemoteException() throws Exception {
        // Configure
        final Bundle getSubscriptionsResponse = new Bundle();
        getSubscriptionsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>());
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>());
        getSubscriptionsResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>());
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getSubscriptionsResponse);
        when(inAppBillingService.getPurchases(3, packageName, "inapp", null)).thenThrow(new RemoteException());

        // Test
        purchaseManager.initialize(application);

        // Verify
        verifyInAppBillingServiceConnected();
        verifyZeroInteractions(purchaseWallet);
    }

    @Test
    public void initializeSubscriptionsHasResponseError() throws Exception {
        // Configure
        final Bundle getSubscriptionsResponse = new Bundle();
        getSubscriptionsResponse.putInt("RESPONSE_CODE", RESULT_ERROR);
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getSubscriptionsResponse);
        final Bundle getConsumablePurchasesResponse = new Bundle();
        getConsumablePurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>());
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>());
        getConsumablePurchasesResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>());
        when(inAppBillingService.getPurchases(3, packageName, "inapp", null)).thenReturn(getConsumablePurchasesResponse);

        // Test
        purchaseManager.initialize(application);

        // Verify
        verifyInAppBillingServiceConnected();
        verifyZeroInteractions(purchaseWallet);
    }

    @Test
    public void initializeConsumablePurchasesHasResponseError() throws Exception {
        // Configure
        final Bundle getSubscriptionsResponse = new Bundle();
        getSubscriptionsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>());
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>());
        getSubscriptionsResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>());
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getSubscriptionsResponse);
        final Bundle getConsumablePurchasesResponse = new Bundle();
        getConsumablePurchasesResponse.putInt("RESPONSE_CODE", RESULT_ERROR);
        when(inAppBillingService.getPurchases(3, packageName, "inapp", null)).thenReturn(getConsumablePurchasesResponse);

        // Test
        purchaseManager.initialize(application);

        // Verify
        verifyInAppBillingServiceConnected();
        verifyZeroInteractions(purchaseWallet);
    }

    @Test
    public void initializeWithNoneOwned() throws Exception {
        // Configure
        final Bundle getSubscriptionsResponse = new Bundle();
        getSubscriptionsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>());
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>());
        getSubscriptionsResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>());
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getSubscriptionsResponse);
        final Bundle getConsumablePurchasesResponse = new Bundle();
        getConsumablePurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>());
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>());
        getConsumablePurchasesResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>());
        when(inAppBillingService.getPurchases(3, packageName, "inapp", null)).thenReturn(getConsumablePurchasesResponse);

        // Test
        purchaseManager.initialize(application);
        verifyInAppBillingServiceConnected();

        verify(purchaseWallet).updatePurchasesInWallet(updateManagedProductsCaptor.capture());
        when(purchaseWallet.getActivePurchases()).thenReturn(updateManagedProductsCaptor.getValue());
        assertEquals(Collections.<ManagedProduct>emptySet(), updateManagedProductsCaptor.getValue());
        verify(purchaseWallet).getActivePurchases();
        verifyNoMoreInteractions(purchaseWallet);
    }

    @Test
    public void initializeWithCancelledConsumablePurchase() throws Exception {
        // Configure
        final Bundle getSubscriptionsResponse = new Bundle();
        getSubscriptionsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>());
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>());
        getSubscriptionsResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>());
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getSubscriptionsResponse);
        final Bundle getConsumablePurchasesResponse = new Bundle();
        getConsumablePurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseItem(InAppPurchase.OcrScans50))));
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseData(InAppPurchase.OcrScans50, PURCHASE_STATE_CANCELLED))));
        getConsumablePurchasesResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>(Collections.singletonList(getInAppDataSignature())));
        when(inAppBillingService.getPurchases(3, packageName, "inapp", null)).thenReturn(getConsumablePurchasesResponse);

        // Test
        purchaseManager.initialize(application);
        verifyInAppBillingServiceConnected();

        verify(purchaseWallet).updatePurchasesInWallet(updateManagedProductsCaptor.capture());
        when(purchaseWallet.getActivePurchases()).thenReturn(updateManagedProductsCaptor.getValue());
        assertEquals(Collections.<ManagedProduct>emptySet(), updateManagedProductsCaptor.getValue());
        verify(purchaseWallet).getActivePurchases();
        verifyNoMoreInteractions(purchaseWallet);
    }

    @Test
    public void initializeWithCancelledSubscription() throws Exception {
        // Configure
        final Bundle getSubscriptionsResponse = new Bundle();
        getSubscriptionsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseItem(InAppPurchase.SmartReceiptsPlus))));
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseData(InAppPurchase.SmartReceiptsPlus, PURCHASE_STATE_CANCELLED))));
        getSubscriptionsResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>(Collections.singletonList(getInAppDataSignature())));
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getSubscriptionsResponse);
        final Bundle getConsumablePurchasesResponse = new Bundle();
        getConsumablePurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>());
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>());
        getConsumablePurchasesResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>());
        when(inAppBillingService.getPurchases(3, packageName, "inapp", null)).thenReturn(getConsumablePurchasesResponse);

        // Test
        purchaseManager.initialize(application);
        verifyInAppBillingServiceConnected();

        verify(purchaseWallet).updatePurchasesInWallet(updateManagedProductsCaptor.capture());
        when(purchaseWallet.getActivePurchases()).thenReturn(updateManagedProductsCaptor.getValue());
        assertEquals(Collections.<ManagedProduct>emptySet(), updateManagedProductsCaptor.getValue());
        verify(purchaseWallet).getActivePurchases();
        verifyNoMoreInteractions(purchaseWallet);
    }

    @Test
    public void initializeWithRefundedConsumablePurchase() throws Exception {
        // Configure
        final Bundle getSubscriptionsResponse = new Bundle();
        getSubscriptionsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>());
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>());
        getSubscriptionsResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>());
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getSubscriptionsResponse);
        final Bundle getConsumablePurchasesResponse = new Bundle();
        getConsumablePurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseItem(InAppPurchase.OcrScans50))));
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseData(InAppPurchase.OcrScans50, PURCHASE_STATE_REFUNDED))));
        getConsumablePurchasesResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>(Collections.singletonList(getInAppDataSignature())));
        when(inAppBillingService.getPurchases(3, packageName, "inapp", null)).thenReturn(getConsumablePurchasesResponse);

        // Test
        purchaseManager.initialize(application);
        verifyInAppBillingServiceConnected();

        verify(purchaseWallet).updatePurchasesInWallet(updateManagedProductsCaptor.capture());
        when(purchaseWallet.getActivePurchases()).thenReturn(updateManagedProductsCaptor.getValue());
        assertEquals(Collections.<ManagedProduct>emptySet(), updateManagedProductsCaptor.getValue());
        verify(purchaseWallet).getActivePurchases();
        verifyNoMoreInteractions(purchaseWallet);
    }

    @Test
    public void initializeWithRefundedSubscription() throws Exception {
        // Configure
        final Bundle getSubscriptionsResponse = new Bundle();
        getSubscriptionsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseItem(InAppPurchase.SmartReceiptsPlus))));
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseData(InAppPurchase.SmartReceiptsPlus, PURCHASE_STATE_REFUNDED))));
        getSubscriptionsResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>(Collections.singletonList(getInAppDataSignature())));
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getSubscriptionsResponse);
        final Bundle getConsumablePurchasesResponse = new Bundle();
        getConsumablePurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>());
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>());
        getConsumablePurchasesResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>());
        when(inAppBillingService.getPurchases(3, packageName, "inapp", null)).thenReturn(getConsumablePurchasesResponse);

        // Test
        purchaseManager.initialize(application);
        verifyInAppBillingServiceConnected();

        verify(purchaseWallet).updatePurchasesInWallet(updateManagedProductsCaptor.capture());
        when(purchaseWallet.getActivePurchases()).thenReturn(updateManagedProductsCaptor.getValue());
        assertEquals(Collections.<ManagedProduct>emptySet(), updateManagedProductsCaptor.getValue());
        verify(purchaseWallet).getActivePurchases();
        verifyNoMoreInteractions(purchaseWallet);
    }

    @Test
    public void initializeWithConsumablePurchasesOwned() throws Exception {
        // Configure
        final Bundle getSubscriptionsResponse = new Bundle();
        getSubscriptionsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>());
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>());
        getSubscriptionsResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>());
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getSubscriptionsResponse);
        final Bundle getConsumablePurchasesResponse = new Bundle();
        getConsumablePurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseItem(InAppPurchase.OcrScans50))));
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseData(InAppPurchase.OcrScans50, PURCHASE_STATE_PURCHASED))));
        getConsumablePurchasesResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>(Collections.singletonList(getInAppDataSignature())));
        when(inAppBillingService.getPurchases(3, packageName, "inapp", null)).thenReturn(getConsumablePurchasesResponse);

        // Test
        purchaseManager.initialize(application);
        verifyInAppBillingServiceConnected();

        final ManagedProduct consumablePurchase = new ConsumablePurchase(InAppPurchase.OcrScans50, getInAppPurchaseData(InAppPurchase.OcrScans50, PURCHASE_STATE_PURCHASED), PURCHASE_TOKEN, IN_APP_DATA_SIGNATURE);
        verify(purchaseWallet).updatePurchasesInWallet(updateManagedProductsCaptor.capture());
        when(purchaseWallet.getActivePurchases()).thenReturn(updateManagedProductsCaptor.getValue());
        assertEquals(Collections.singleton(consumablePurchase), updateManagedProductsCaptor.getValue());
        verify(purchaseWallet).getActivePurchases();
        verifyNoMoreInteractions(purchaseWallet);
    }

    @Test
    public void initializeWithSubscriptionsOwned() throws Exception {
        // Configure
        final Bundle getSubscriptionsResponse = new Bundle();
        getSubscriptionsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseItem(InAppPurchase.SmartReceiptsPlus))));
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseData(InAppPurchase.SmartReceiptsPlus, PURCHASE_STATE_PURCHASED))));
        getSubscriptionsResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>(Collections.singletonList(getInAppDataSignature())));
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getSubscriptionsResponse);
        final Bundle getConsumablePurchasesResponse = new Bundle();
        getConsumablePurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>());
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>());
        getConsumablePurchasesResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>());
        when(inAppBillingService.getPurchases(3, packageName, "inapp", null)).thenReturn(getConsumablePurchasesResponse);

        // Test
        purchaseManager.initialize(application);
        verifyInAppBillingServiceConnected();

        final ManagedProduct subscription = new Subscription(InAppPurchase.SmartReceiptsPlus, getInAppPurchaseData(InAppPurchase.SmartReceiptsPlus, PURCHASE_STATE_PURCHASED), PURCHASE_TOKEN, IN_APP_DATA_SIGNATURE);
        verify(purchaseWallet).updatePurchasesInWallet(updateManagedProductsCaptor.capture());
        when(purchaseWallet.getActivePurchases()).thenReturn(updateManagedProductsCaptor.getValue());
        assertEquals(Collections.singleton(subscription), updateManagedProductsCaptor.getValue());
        verify(purchaseWallet).getActivePurchases();
        verifyNoMoreInteractions(purchaseWallet);
    }

    @Test
    public void initializeWithSubscriptionsAndConsumablePurchasesOwned() throws Exception {
        // Configure
        final Bundle getSubscriptionsResponse = new Bundle();
        getSubscriptionsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseItem(InAppPurchase.SmartReceiptsPlus))));
        getSubscriptionsResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseData(InAppPurchase.SmartReceiptsPlus, PURCHASE_STATE_PURCHASED))));
        getSubscriptionsResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>(Collections.singletonList(getInAppDataSignature())));
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getSubscriptionsResponse);
        final Bundle getConsumablePurchasesResponse = new Bundle();
        getConsumablePurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseItem(InAppPurchase.OcrScans50))));
        getConsumablePurchasesResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseData(InAppPurchase.OcrScans50, PURCHASE_STATE_PURCHASED))));
        getConsumablePurchasesResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>(Collections.singletonList(getInAppDataSignature())));
        when(inAppBillingService.getPurchases(3, packageName, "inapp", null)).thenReturn(getConsumablePurchasesResponse);

        // Test
        purchaseManager.initialize(application);
        verifyInAppBillingServiceConnected();

        final ManagedProduct subscription = new Subscription(InAppPurchase.SmartReceiptsPlus, getInAppPurchaseData(InAppPurchase.SmartReceiptsPlus, PURCHASE_STATE_PURCHASED), PURCHASE_TOKEN, IN_APP_DATA_SIGNATURE);
        final ManagedProduct consumablePurchase = new ConsumablePurchase(InAppPurchase.OcrScans50, getInAppPurchaseData(InAppPurchase.OcrScans50, PURCHASE_STATE_PURCHASED), PURCHASE_TOKEN, IN_APP_DATA_SIGNATURE);
        verify(purchaseWallet).updatePurchasesInWallet(updateManagedProductsCaptor.capture());
        when(purchaseWallet.getActivePurchases()).thenReturn(updateManagedProductsCaptor.getValue());
        assertEquals(new HashSet<>(Arrays.asList(subscription, consumablePurchase)), updateManagedProductsCaptor.getValue());
        verify(purchaseWallet).getActivePurchases();
        verifyNoMoreInteractions(purchaseWallet);
    }

    @Test
    public void getAvailableSubscriptionsThrowsRemoteException() throws Exception {
        // Configure
        final Bundle getSkuDetailsResponse = new Bundle();
        getSkuDetailsResponse.putInt("RESPONSE_CODE", RESULT_ERROR);
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("subs"), bundleCaptor.capture())).thenThrow(new RemoteException());

        // Test
        purchaseManager.getAvailableSubscriptions().test()
                // Verify
                .assertNoValues()
                .assertNotComplete()
                .assertError(RemoteException.class);
        verifyZeroInteractions(purchaseWallet);
        assertEquals(bundleCaptor.getValue().getStringArrayList("ITEM_ID_LIST"), InAppPurchase.getSubscriptionSkus());
    }

    @Test
    public void getAvailableSubscriptionsResponseError() throws Exception {
        // Configure
        final Bundle getSkuDetailsResponse = new Bundle();
        getSkuDetailsResponse.putInt("RESPONSE_CODE", RESULT_ERROR);
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("subs"), bundleCaptor.capture())).thenReturn(getSkuDetailsResponse);

        // Test
        purchaseManager.getAvailableSubscriptions().test()

                // Verify
                .assertNoValues()
                .assertNotComplete()
                .assertError(Exception.class);
        verifyZeroInteractions(purchaseWallet);
        assertEquals(bundleCaptor.getValue().getStringArrayList("ITEM_ID_LIST"), InAppPurchase.getSubscriptionSkus());
    }

    @Test
    public void getAvailableSubscriptionsWithNoneAvailable() throws Exception {
        // Configure
        final Bundle getSkuDetailsResponse = new Bundle();
        getSkuDetailsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSkuDetailsResponse.putStringArrayList("DETAILS_LIST", new ArrayList<>());
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("subs"), bundleCaptor.capture())).thenReturn(getSkuDetailsResponse);

        // Test
        purchaseManager.getAvailableSubscriptions().test()

                // Verify
                .assertValue(Collections.<AvailablePurchase>emptySet())
                .assertComplete()
                .assertNoErrors();
        verify(purchaseWallet, never()).addPurchaseToWallet(any(ManagedProduct.class));
        verify(purchaseWallet, never()).updatePurchasesInWallet(anySetOf(ManagedProduct.class));
        verify(purchaseWallet, never()).removePurchaseFromWallet(any(InAppPurchase.class));
        assertEquals(bundleCaptor.getValue().getStringArrayList("ITEM_ID_LIST"), InAppPurchase.getSubscriptionSkus());
    }

    @Test
    public void getAvailableSubscriptionsWithSomeAvailable() throws Exception {
        // Configure
        final Bundle getSkuDetailsResponse = new Bundle();
        getSkuDetailsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSkuDetailsResponse.putStringArrayList("DETAILS_LIST", new ArrayList<>(Collections.singletonList(getSkuDetails(InAppPurchase.SmartReceiptsPlus))));
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("subs"), bundleCaptor.capture())).thenReturn(getSkuDetailsResponse);

        // Test
        purchaseManager.getAvailableSubscriptions().test()

                // Verify
                .assertValue(Collections.singleton(getSkuDetailsAsAvailablePurchase(InAppPurchase.SmartReceiptsPlus)))
                .assertComplete()
                .assertNoErrors();
        verify(purchaseWallet, never()).addPurchaseToWallet(any(ManagedProduct.class));
        verify(purchaseWallet, never()).updatePurchasesInWallet(anySetOf(ManagedProduct.class));
        verify(purchaseWallet, never()).removePurchaseFromWallet(any(InAppPurchase.class));
        assertEquals(bundleCaptor.getValue().getStringArrayList("ITEM_ID_LIST"), InAppPurchase.getSubscriptionSkus());
    }

    @Test
    public void getAvailableConsumablePurchasesThrowsRemoteException() throws Exception {
        // Configure
        final Bundle getSkuDetailsResponse = new Bundle();
        getSkuDetailsResponse.putInt("RESPONSE_CODE", RESULT_ERROR);
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("inapp"), bundleCaptor.capture())).thenThrow(new RemoteException());

        // Test
        purchaseManager.getAvailableConsumablePurchases().test()

                // Verify
                .assertNoValues()
                .assertNotComplete()
                .assertError(RemoteException.class);
        verifyZeroInteractions(purchaseWallet);
        assertEquals(bundleCaptor.getValue().getStringArrayList("ITEM_ID_LIST"), InAppPurchase.getConsumablePurchaseSkus());
    }

    @Test
    public void getAvailableConsumablePurchasesResponseError() throws Exception {
        // Configure
        final Bundle getSkuDetailsResponse = new Bundle();
        getSkuDetailsResponse.putInt("RESPONSE_CODE", RESULT_ERROR);
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("inapp"), bundleCaptor.capture())).thenReturn(getSkuDetailsResponse);

        // Test
        purchaseManager.getAvailableConsumablePurchases().test()

                // Verify
                .assertNoValues()
                .assertNotComplete()
                .assertError(Exception.class);
        verifyZeroInteractions(purchaseWallet);
        assertEquals(bundleCaptor.getValue().getStringArrayList("ITEM_ID_LIST"), InAppPurchase.getConsumablePurchaseSkus());
    }

    @Test
    public void getAvailableConsumablePurchasesWithNoneAvailable() throws Exception {
        // Configure
        final Bundle getSkuDetailsResponse = new Bundle();
        getSkuDetailsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSkuDetailsResponse.putStringArrayList("DETAILS_LIST", new ArrayList<>());
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("inapp"), bundleCaptor.capture())).thenReturn(getSkuDetailsResponse);

        // Test
        purchaseManager.getAvailableConsumablePurchases().test()

                // Verify
                .assertValue(Collections.<AvailablePurchase>emptySet())
                .assertComplete()
                .assertNoErrors();
        verify(purchaseWallet, never()).addPurchaseToWallet(any(ManagedProduct.class));
        verify(purchaseWallet, never()).updatePurchasesInWallet(anySetOf(ManagedProduct.class));
        verify(purchaseWallet, never()).removePurchaseFromWallet(any(InAppPurchase.class));
        assertEquals(bundleCaptor.getValue().getStringArrayList("ITEM_ID_LIST"), InAppPurchase.getConsumablePurchaseSkus());
    }

    @Test
    public void getAvailableConsumablePurchasesWithSomeAvailable() throws Exception {
        // Configure
        final Bundle getSkuDetailsResponse = new Bundle();
        getSkuDetailsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSkuDetailsResponse.putStringArrayList("DETAILS_LIST", new ArrayList<>(Collections.singletonList(getSkuDetails(InAppPurchase.OcrScans50))));
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("inapp"), bundleCaptor.capture())).thenReturn(getSkuDetailsResponse);

        // Test
        purchaseManager.getAvailableConsumablePurchases().test()

                // Verify
                .assertValue(Collections.singleton(getSkuDetailsAsAvailablePurchase(InAppPurchase.OcrScans50)))
                .assertComplete()
                .assertNoErrors();
        verify(purchaseWallet, never()).addPurchaseToWallet(any(ManagedProduct.class));
        verify(purchaseWallet, never()).updatePurchasesInWallet(anySetOf(ManagedProduct.class));
        verify(purchaseWallet, never()).removePurchaseFromWallet(any(InAppPurchase.class));
        assertEquals(bundleCaptor.getValue().getStringArrayList("ITEM_ID_LIST"), InAppPurchase.getConsumablePurchaseSkus());
    }

    @Test
    public void getAllAvailablePurchases() throws Exception {
        // Configure
        final Bundle getSubscriptionsResponse = new Bundle();
        getSubscriptionsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSubscriptionsResponse.putStringArrayList("DETAILS_LIST", new ArrayList<>(Collections.singletonList(getSkuDetails(InAppPurchase.SmartReceiptsPlus))));
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("subs"), bundleCaptor.capture())).thenReturn(getSubscriptionsResponse);
        final Bundle getConsumablePurchasesResponse = new Bundle();
        getConsumablePurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getConsumablePurchasesResponse.putStringArrayList("DETAILS_LIST", new ArrayList<>(Collections.singletonList(getSkuDetails(InAppPurchase.OcrScans50))));
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("inapp"), bundleCaptor.capture())).thenReturn(getConsumablePurchasesResponse);

        // Test
        purchaseManager.getAllAvailablePurchases().test()
                .assertValue(new HashSet<>(Arrays.asList(getSkuDetailsAsAvailablePurchase(InAppPurchase.SmartReceiptsPlus), getSkuDetailsAsAvailablePurchase(InAppPurchase.OcrScans50))))
                .assertComplete()
                .assertNoErrors();
        verify(purchaseWallet, never()).addPurchaseToWallet(any(ManagedProduct.class));
        verify(purchaseWallet, never()).updatePurchasesInWallet(anySetOf(ManagedProduct.class));
        verify(purchaseWallet, never()).removePurchaseFromWallet(any(InAppPurchase.class));
    }

    @Test
    public void getAllAvailablePurchaseSkus() throws Exception {
        // Configure
        final Bundle getSubscriptionsResponse = new Bundle();
        getSubscriptionsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSubscriptionsResponse.putStringArrayList("DETAILS_LIST", new ArrayList<>(Collections.singletonList(getSkuDetails(InAppPurchase.SmartReceiptsPlus))));
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("subs"), bundleCaptor.capture())).thenReturn(getSubscriptionsResponse);
        final Bundle getConsumablePurchasesResponse = new Bundle();
        getConsumablePurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getConsumablePurchasesResponse.putStringArrayList("DETAILS_LIST", new ArrayList<>(Collections.singletonList(getSkuDetails(InAppPurchase.OcrScans50))));
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("inapp"), bundleCaptor.capture())).thenReturn(getConsumablePurchasesResponse);

        // Test
        purchaseManager.getAllAvailablePurchaseSkus().test()
                .assertValue(new HashSet<>(Arrays.asList(InAppPurchase.SmartReceiptsPlus, InAppPurchase.OcrScans50)))
                .assertComplete()
                .assertNoErrors();
        verify(purchaseWallet, never()).addPurchaseToWallet(any(ManagedProduct.class));
        verify(purchaseWallet, never()).updatePurchasesInWallet(anySetOf(ManagedProduct.class));
        verify(purchaseWallet, never()).removePurchaseFromWallet(any(InAppPurchase.class));
    }

    @Test
    public void consumePurchaseThrowsRemoteException() throws Exception {
        // Configure
        when(consumablePurchase.getPurchaseToken()).thenReturn(PURCHASE_TOKEN);
        when(inAppBillingService.consumePurchase(3, packageName, PURCHASE_TOKEN)).thenThrow(new RemoteException());

        // Test
        purchaseManager.consumePurchase(consumablePurchase).test()

                // Verify
                .assertNoValues()
                .assertNotComplete()
                .assertError(RemoteException.class);
    }

    @Test
    public void consumePurchaseFails() throws Exception {
        // Configure
        when(consumablePurchase.getPurchaseToken()).thenReturn(PURCHASE_TOKEN);
        when(inAppBillingService.consumePurchase(3, packageName, PURCHASE_TOKEN)).thenReturn(RESULT_ERROR);

        // Test
        purchaseManager.consumePurchase(consumablePurchase).test()

                // Verify
                .assertNoValues()
                .assertNotComplete()
                .assertError(Exception.class);
    }

    @Test
    public void consumePurchaseSucceeds() throws Exception {
        // Configure
        when(consumablePurchase.getPurchaseToken()).thenReturn(PURCHASE_TOKEN);
        when(inAppBillingService.consumePurchase(3, packageName, PURCHASE_TOKEN)).thenReturn(RESULT_OK);

        // Test
        purchaseManager.consumePurchase(consumablePurchase).test()

                // Verify
                .assertNoValues()
                .assertComplete()
                .assertNoErrors();
    }

    private void verifyInAppBillingServiceConnected() {
        final Intent intent = shadowApplication.getNextStartedService();
        assertNotNull(intent);
        assertEquals(intent.getAction(), "com.android.vending.billing.InAppBillingService.BIND");
        assertFalse(shadowApplication.getBoundServiceConnections().isEmpty());
        // TODO: Verify activity life-cycle callbacks are working with custom shadow
    }

    @NonNull
    private String getInAppPurchaseItem(@NonNull InAppPurchase inAppPurchase) throws Exception {
        return inAppPurchase.getSku();
    }

    @NonNull
    private String getInAppPurchaseData(@NonNull InAppPurchase inAppPurchase, int purchaseState) throws Exception {
        // https://developer.android.com/google/play/billing/billing_reference.html
        final JSONObject json = new JSONObject();
        if (Subscription.class.equals(inAppPurchase.getType())) {
            json.put("autoRenewing", true);
        }
        json.put("orderId", "orderId");
        json.put("packageName", packageName);
        json.put("productId", inAppPurchase.getSku());
        json.put("purchaseTime", 1234567890123L);
        json.put("purchaseState", purchaseState);
        json.put("developerPayload", "1234567890");
        json.put("purchaseToken", PURCHASE_TOKEN);
        return json.toString();
    }

    @NonNull
    private String getInAppDataSignature() throws Exception {
        return IN_APP_DATA_SIGNATURE;
    }

    @NonNull
    private String getSkuDetails(@NonNull InAppPurchase inAppPurchase) throws Exception {
        // https://developer.android.com/google/play/billing/billing_reference.html
        final JSONObject json = new JSONObject();
        json.put("productId", inAppPurchase.getSku());
        json.put("type", "subs"); //TODO
        json.put("price", "$4.99");
        json.put("price_amount_micros", 4990000);
        json.put("price_currency_code", "USD");
        json.put("title", "title");
        json.put("description", "description");
        return json.toString();
    }

    @NonNull
    private AvailablePurchase getSkuDetailsAsAvailablePurchase(@NonNull InAppPurchase inAppPurchase) throws Exception {
        return new Gson().fromJson(getSkuDetails(inAppPurchase), AvailablePurchase.class);
    }
}
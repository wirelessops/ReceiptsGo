package co.smartreceipts.android.purchases.wallet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;
import java.util.Date;

import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.model.Subscription;
import co.smartreceipts.android.purchases.subscriptions.RemoteSubscription;
import dagger.Lazy;

@RunWith(RobolectricTestRunner.class)
public class DefaultPurchaseWalletTest {

    private static final String TEST = "test";
    private static final String PURCHASE_TOKEN = "012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689";
    private static final String IN_APP_DATA_SIGNATURE = "012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689==";
    private String purchaseData;

    // Class under test
    DefaultPurchaseWallet defaultPurchaseWallet;

    SharedPreferences preferences;

    ManagedProduct managedProduct;

    @Mock
    Lazy<SharedPreferences> sharedPreferencesLazy;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        // https://developer.android.com/google/play/billing/billing_reference.html
        final JSONObject purchaseData = new JSONObject();
        purchaseData.put("autoRenewing", true);
        purchaseData.put("orderId", "orderId");
        purchaseData.put("packageName", "co.smartreceipts.android");
        purchaseData.put("productId", InAppPurchase.SmartReceiptsPlus.getSku());
        purchaseData.put("purchaseTime", 1234567890123L);
        purchaseData.put("purchaseState", 0);
        purchaseData.put("developerPayload", "1234567890");
        purchaseData.put("purchaseToken", PURCHASE_TOKEN);
        this.purchaseData = purchaseData.toString();

        managedProduct = new Subscription(InAppPurchase.SmartReceiptsPlus, this.purchaseData, PURCHASE_TOKEN, IN_APP_DATA_SIGNATURE);

        preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext());
        preferences.edit().putString(TEST, TEST).apply();

        when(sharedPreferencesLazy.get()).thenReturn(preferences);

        defaultPurchaseWallet = new DefaultPurchaseWallet(sharedPreferencesLazy);
    }

    @After
    public void tearDown() {
        // Verify that we don't clear out everything then remove our test values
        assertEquals(TEST, preferences.getString(TEST, null));
        preferences.edit().clear().apply();
    }

    @Test
    public void emptyPurchases() {
        assertFalse(defaultPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertNull(defaultPurchaseWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertEquals(defaultPurchaseWallet.getActiveLocalInAppPurchases(), Collections.<ManagedProduct>emptySet());
    }

    @Test
    public void singlePurchase() {
        defaultPurchaseWallet.addLocalInAppPurchaseToWallet(managedProduct);

        assertTrue(defaultPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertEquals(managedProduct, defaultPurchaseWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertEquals(defaultPurchaseWallet.getActiveLocalInAppPurchases(), Collections.singleton(managedProduct));
        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.singleton(InAppPurchase.SmartReceiptsPlus.getSku()));
        assertEquals(preferences.getString("plus_sku_4_purchaseData", null), purchaseData);
        assertEquals(preferences.getString("plus_sku_4_inAppDataSignature", null), IN_APP_DATA_SIGNATURE);
    }

    @Test
    public void updatePurchases() {
        defaultPurchaseWallet.updateLocalInAppPurchasesInWallet(Collections.singleton(managedProduct));

        assertTrue(defaultPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertEquals(managedProduct, defaultPurchaseWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertEquals(defaultPurchaseWallet.getActiveLocalInAppPurchases(), Collections.singleton(managedProduct));
        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.singleton(InAppPurchase.SmartReceiptsPlus.getSku()));
        assertEquals(preferences.getString("plus_sku_4_purchaseData", null), purchaseData);
        assertEquals(preferences.getString("plus_sku_4_inAppDataSignature", null), IN_APP_DATA_SIGNATURE);
    }

    @Test
    public void updateRemotePurchases() {
        assertFalse(defaultPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));

        final RemoteSubscription remoteSubscription = new RemoteSubscription(1, InAppPurchase.SmartReceiptsPlus, new Date());
        defaultPurchaseWallet.updateRemotePurchases(Collections.singleton(remoteSubscription));
        assertTrue(defaultPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));

        // Now ensure persisted
        final PurchaseWallet newWallet = new DefaultPurchaseWallet(sharedPreferencesLazy);
        assertTrue(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
    }

    @Test
    public void ensureAddedPurchaseIsPersisted() {
        defaultPurchaseWallet.addLocalInAppPurchaseToWallet(managedProduct);
        final PurchaseWallet newWallet = new DefaultPurchaseWallet(sharedPreferencesLazy);

        assertTrue(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertTrue(defaultPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertEquals(managedProduct, newWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertEquals(managedProduct, defaultPurchaseWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertEquals(newWallet.getActiveLocalInAppPurchases(), Collections.singleton(managedProduct));
        assertEquals(defaultPurchaseWallet.getActiveLocalInAppPurchases(), Collections.singleton(managedProduct));
        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.singleton(InAppPurchase.SmartReceiptsPlus.getSku()));
        assertEquals(preferences.getString("plus_sku_4_purchaseData", null), purchaseData);
        assertEquals(preferences.getString("plus_sku_4_inAppDataSignature", null), IN_APP_DATA_SIGNATURE);
    }

    @Test
    public void ensureUpdatedPurchaseListIsPersisted() {
        // First add it
        defaultPurchaseWallet.addLocalInAppPurchaseToWallet(managedProduct);

        // Then revoke it
        defaultPurchaseWallet.updateLocalInAppPurchasesInWallet(Collections.<ManagedProduct>emptySet());
        final PurchaseWallet newWallet = new DefaultPurchaseWallet(sharedPreferencesLazy);

        assertFalse(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertFalse(defaultPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertNull(newWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertNull(defaultPurchaseWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertEquals(newWallet.getActiveLocalInAppPurchases(), Collections.<ManagedProduct>emptySet());
        assertEquals(defaultPurchaseWallet.getActiveLocalInAppPurchases(), Collections.<ManagedProduct>emptySet());
        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.emptySet());
        assertFalse(preferences.contains("plus_sku_4_purchaseData"));
        assertFalse(preferences.contains("plus_sku_4_inAppDataSignature"));
    }

    @Test
    public void upgradeFrom_V_4_2_0_249_WhenWeDidNotPersistDataOrSignature() {
        // Historically, we only used to save the sku set and not the token or signature
        preferences.edit().putStringSet("key_sku_set", Collections.singleton(InAppPurchase.SmartReceiptsPlus.getSku())).apply();
        final PurchaseWallet newWallet = new DefaultPurchaseWallet(sharedPreferencesLazy);

        assertTrue(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        final ManagedProduct partialManagedProduct = newWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus);
        assertNotNull(partialManagedProduct);
        assertTrue(!partialManagedProduct.equals(managedProduct));
        assertEquals(partialManagedProduct.getInAppPurchase(), InAppPurchase.SmartReceiptsPlus);
        assertEquals(partialManagedProduct.getInAppDataSignature(), "");
        assertEquals(partialManagedProduct.getPurchaseToken(), "");
        assertEquals(partialManagedProduct.getPurchaseData(), "");
        assertEquals(newWallet.getActiveLocalInAppPurchases(), Collections.singleton(partialManagedProduct));

        // Update from Google InAppBilling
        newWallet.updateLocalInAppPurchasesInWallet(Collections.singleton(managedProduct));

        // Verify that we've now save the extra params
        assertEquals(preferences.getString("plus_sku_4_purchaseData", null), purchaseData);
        assertEquals(preferences.getString("plus_sku_4_inAppDataSignature", null), IN_APP_DATA_SIGNATURE);
        assertEquals(managedProduct, newWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertEquals(newWallet.getActiveLocalInAppPurchases(), Collections.singleton(managedProduct));
    }

}
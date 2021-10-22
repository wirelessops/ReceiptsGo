package co.smartreceipts.android.purchases.wallet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

import co.smartreceipts.android.purchases.model.ConsumablePurchase;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import dagger.Lazy;

@RunWith(RobolectricTestRunner.class)
public class PlusPurchaseWalletTest {

    private static final String TEST = "test";
    private static final String PURCHASE_TOKEN = "012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689";
    private static final String IN_APP_DATA_SIGNATURE = "012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689==";
    private String purchaseData;

    // Class under test
    PlusPurchaseWallet plusPurchaseWallet;

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
        purchaseData.put("productId", InAppPurchase.OcrScans50.getSku());
        purchaseData.put("purchaseTime", 1234567890123L);
        purchaseData.put("purchaseState", 0);
        purchaseData.put("developerPayload", "1234567890");
        purchaseData.put("purchaseToken", PURCHASE_TOKEN);
        this.purchaseData = purchaseData.toString();

        managedProduct = new ConsumablePurchase(InAppPurchase.OcrScans50, this.purchaseData, PURCHASE_TOKEN, IN_APP_DATA_SIGNATURE);

        preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext());
        preferences.edit().putString(TEST, TEST).apply();

        when(sharedPreferencesLazy.get()).thenReturn(preferences);

        plusPurchaseWallet = new PlusPurchaseWallet(sharedPreferencesLazy);
    }

    @After
    public void tearDown() {
        // Verify that we don't clear out everything then remove our test values
        assertEquals(TEST, preferences.getString(TEST, null));
        preferences.edit().clear().apply();
    }

    @Test
    public void emptyPurchases() {
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertNull(plusPurchaseWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertFalse(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertNull(plusPurchaseWallet.getLocalInAppManagedProduct(InAppPurchase.OcrScans50));
        assertEquals(plusPurchaseWallet.getActiveLocalInAppPurchases(), Collections.emptySet());
    }

    @Test
    public void singlePurchase() {
        plusPurchaseWallet.addLocalInAppPurchaseToWallet(managedProduct);

        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertNull(plusPurchaseWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertEquals(managedProduct, plusPurchaseWallet.getLocalInAppManagedProduct(InAppPurchase.OcrScans50));
        assertEquals(plusPurchaseWallet.getActiveLocalInAppPurchases(), Collections.singleton(managedProduct));

        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.singleton(InAppPurchase.OcrScans50.getSku()));
        assertEquals(preferences.getString("ocr_purchase_1_purchaseData", null), this.purchaseData);
        assertEquals(preferences.getString("ocr_purchase_1_inAppDataSignature", null), IN_APP_DATA_SIGNATURE);
    }

    @Test
    public void updatePurchases() {
        plusPurchaseWallet.updateLocalInAppPurchasesInWallet(Collections.singleton(managedProduct));

        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertEquals(plusPurchaseWallet.getActiveLocalInAppPurchases(), Collections.singleton(managedProduct));

        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.singleton(InAppPurchase.OcrScans50.getSku()));
        assertEquals(preferences.getString("ocr_purchase_1_purchaseData", null), this.purchaseData);
        assertEquals(preferences.getString("ocr_purchase_1_inAppDataSignature", null), IN_APP_DATA_SIGNATURE);
    }

    @Test
    public void ensureAddedPurchaseIsPersistedAndPlusRemains() {
        plusPurchaseWallet.addLocalInAppPurchaseToWallet(managedProduct);
        final PurchaseWallet newWallet = new PlusPurchaseWallet(sharedPreferencesLazy);

        assertTrue(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertNull(newWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertNull(plusPurchaseWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus));

        assertTrue(newWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertEquals(managedProduct, newWallet.getLocalInAppManagedProduct(InAppPurchase.OcrScans50));
        assertEquals(newWallet.getActiveLocalInAppPurchases(), Collections.singleton(managedProduct));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertEquals(managedProduct, plusPurchaseWallet.getLocalInAppManagedProduct(InAppPurchase.OcrScans50));
        assertEquals(plusPurchaseWallet.getActiveLocalInAppPurchases(), Collections.singleton(managedProduct));

        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.singleton(InAppPurchase.OcrScans50.getSku()));
        assertEquals(preferences.getString("ocr_purchase_1_purchaseData", null), this.purchaseData);
        assertEquals(preferences.getString("ocr_purchase_1_inAppDataSignature", null), IN_APP_DATA_SIGNATURE);
    }

    @Test
    public void ensureUpdatedPurchaseListIsPersistedAndPlusRemains() {
        // First add it
        plusPurchaseWallet.addLocalInAppPurchaseToWallet(managedProduct);

        // Then revoke it
        plusPurchaseWallet.updateLocalInAppPurchasesInWallet(Collections.<ManagedProduct>emptySet());
        final PurchaseWallet newWallet = new PlusPurchaseWallet(sharedPreferencesLazy);

        assertTrue(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertNull(newWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertNull(plusPurchaseWallet.getLocalInAppManagedProduct(InAppPurchase.SmartReceiptsPlus));

        assertFalse(newWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertNull(newWallet.getLocalInAppManagedProduct(InAppPurchase.OcrScans50));
        assertFalse(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertNull(plusPurchaseWallet.getLocalInAppManagedProduct(InAppPurchase.OcrScans50));

        assertEquals(newWallet.getActiveLocalInAppPurchases(), Collections.emptySet());
        assertEquals(plusPurchaseWallet.getActiveLocalInAppPurchases(), Collections.emptySet());

        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.emptySet());
        assertFalse(preferences.contains("ocr_purchase_1_purchaseData"));
        assertFalse(preferences.contains("ocr_purchase_1_inAppDataSignature"));
    }

}
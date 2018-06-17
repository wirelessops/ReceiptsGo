package co.smartreceipts.android.purchases.wallet;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import co.smartreceipts.android.purchases.model.ConsumablePurchase;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.model.Subscription;
import dagger.Lazy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

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

    ManagedProduct plusManagedProduct;

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
        plusManagedProduct = new Subscription(InAppPurchase.SmartReceiptsPlus, "", "", "");

        preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
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
        assertEquals(plusManagedProduct, plusPurchaseWallet.getManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertFalse(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertNull(plusPurchaseWallet.getManagedProduct(InAppPurchase.OcrScans50));
        assertEquals(plusPurchaseWallet.getActivePurchases(), Collections.singleton(plusManagedProduct));
    }

    @Test
    public void singlePurchase() {
        plusPurchaseWallet.addPurchaseToWallet(managedProduct);

        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertEquals(plusManagedProduct, plusPurchaseWallet.getManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertEquals(managedProduct, plusPurchaseWallet.getManagedProduct(InAppPurchase.OcrScans50));
        assertEquals(plusPurchaseWallet.getActivePurchases(), new HashSet<>(Arrays.asList(managedProduct, plusManagedProduct)));

        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.singleton(InAppPurchase.OcrScans50.getSku()));
        assertEquals(preferences.getString("ocr_purchase_1_purchaseData", null), this.purchaseData);
        assertEquals(preferences.getString("ocr_purchase_1_inAppDataSignature", null), IN_APP_DATA_SIGNATURE);
    }

    @Test
    public void updatePurchases() {
        plusPurchaseWallet.updatePurchasesInWallet(Collections.singleton(managedProduct));

        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertEquals(plusPurchaseWallet.getActivePurchases(), new HashSet<>(Arrays.asList(managedProduct, plusManagedProduct)));

        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.singleton(InAppPurchase.OcrScans50.getSku()));
        assertEquals(preferences.getString("ocr_purchase_1_purchaseData", null), this.purchaseData);
        assertEquals(preferences.getString("ocr_purchase_1_inAppDataSignature", null), IN_APP_DATA_SIGNATURE);
    }

    @Test
    public void removeMissingPurchase() {
        plusPurchaseWallet.removePurchaseFromWallet(InAppPurchase.OcrScans50);

        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertEquals(plusManagedProduct, plusPurchaseWallet.getManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertFalse(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertNull(plusPurchaseWallet.getManagedProduct(InAppPurchase.OcrScans50));
        assertEquals(plusPurchaseWallet.getActivePurchases(), Collections.singleton(plusManagedProduct));

        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.<String>emptySet());
        assertFalse(preferences.contains("ocr_purchase_1_purchaseData"));
        assertFalse(preferences.contains("ocr_purchase_1_inAppDataSignature"));
    }

    @Test
    public void ensureAddedPurchaseIsPersistedAndPlusRemains() {
        plusPurchaseWallet.addPurchaseToWallet(managedProduct);
        final PurchaseWallet newWallet = new PlusPurchaseWallet(sharedPreferencesLazy);

        assertTrue(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertEquals(plusManagedProduct, newWallet.getManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertEquals(plusManagedProduct, plusPurchaseWallet.getManagedProduct(InAppPurchase.SmartReceiptsPlus));

        assertTrue(newWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertEquals(managedProduct, newWallet.getManagedProduct(InAppPurchase.OcrScans50));
        assertEquals(newWallet.getActivePurchases(), new HashSet<>(Arrays.asList(managedProduct, plusManagedProduct)));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertEquals(managedProduct, plusPurchaseWallet.getManagedProduct(InAppPurchase.OcrScans50));
        assertEquals(plusPurchaseWallet.getActivePurchases(), new HashSet<>(Arrays.asList(managedProduct, plusManagedProduct)));

        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.singleton(InAppPurchase.OcrScans50.getSku()));
        assertEquals(preferences.getString("ocr_purchase_1_purchaseData", null), this.purchaseData);
        assertEquals(preferences.getString("ocr_purchase_1_inAppDataSignature", null), IN_APP_DATA_SIGNATURE);
    }

    @Test
    public void ensureUpdatedPurchaseListIsPersistedAndPlusRemains() {
        // First add it
        plusPurchaseWallet.addPurchaseToWallet(managedProduct);

        // Then revoke it
        plusPurchaseWallet.updatePurchasesInWallet(Collections.<ManagedProduct>emptySet());
        final PurchaseWallet newWallet = new PlusPurchaseWallet(sharedPreferencesLazy);

        assertTrue(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertEquals(plusManagedProduct, newWallet.getManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertEquals(plusManagedProduct, plusPurchaseWallet.getManagedProduct(InAppPurchase.SmartReceiptsPlus));

        assertFalse(newWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertNull(newWallet.getManagedProduct(InAppPurchase.OcrScans50));
        assertFalse(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertNull(plusPurchaseWallet.getManagedProduct(InAppPurchase.OcrScans50));

        assertEquals(newWallet.getActivePurchases(), Collections.singleton(plusManagedProduct));
        assertEquals(plusPurchaseWallet.getActivePurchases(), Collections.singleton(plusManagedProduct));

        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.emptySet());
        assertFalse(preferences.contains("ocr_purchase_1_purchaseData"));
        assertFalse(preferences.contains("ocr_purchase_1_inAppDataSignature"));
    }

    @Test
    public void ensureRemovedPurchaseIsPersisted() {
        plusPurchaseWallet.addPurchaseToWallet(managedProduct);
        plusPurchaseWallet.removePurchaseFromWallet(InAppPurchase.OcrScans50);

        final PurchaseWallet newWallet = new PlusPurchaseWallet(sharedPreferencesLazy);

        assertTrue(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertEquals(plusManagedProduct, newWallet.getManagedProduct(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertEquals(plusManagedProduct, plusPurchaseWallet.getManagedProduct(InAppPurchase.SmartReceiptsPlus));

        assertFalse(newWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertNull(newWallet.getManagedProduct(InAppPurchase.OcrScans50));
        assertFalse(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertNull(plusPurchaseWallet.getManagedProduct(InAppPurchase.OcrScans50));

        assertEquals(newWallet.getActivePurchases(), Collections.singleton(plusManagedProduct));
        assertEquals(plusPurchaseWallet.getActivePurchases(), Collections.singleton(plusManagedProduct));

        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.emptySet());
        assertFalse(preferences.contains("ocr_purchase_1_purchaseData"));
        assertFalse(preferences.contains("ocr_purchase_1_inAppDataSignature"));
    }

}
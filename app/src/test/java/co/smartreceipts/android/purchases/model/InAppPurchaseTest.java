package co.smartreceipts.android.purchases.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class InAppPurchaseTest {

    @Test
    public void getters() {
        assertEquals(InAppPurchase.SmartReceiptsPlus.getSku(), "plus_sku_5");
        assertEquals(InAppPurchase.SmartReceiptsPlus.getType(), Subscription.class);
        assertEquals(InAppPurchase.SmartReceiptsPlus.getProductType(), "subs");
        assertEquals(InAppPurchase.SmartReceiptsPlus.getPurchaseFamilies(), new HashSet<>(Arrays.asList(PurchaseFamily.SmartReceiptsPlus, PurchaseFamily.Ocr)));
        assertEquals(InAppPurchase.SmartReceiptsPlus.getLegacySkus(), new HashSet<>(Arrays.asList("pro_sku_3", "plus_sku_4")));

        assertEquals(InAppPurchase.OcrScans50.getSku(), "ocr_purchase_1");
        assertEquals(InAppPurchase.OcrScans50.getType(), ConsumablePurchase.class);
        assertEquals(InAppPurchase.OcrScans50.getProductType(), "inapp");
        assertEquals(InAppPurchase.OcrScans50.getPurchaseFamilies(), Collections.singleton(PurchaseFamily.Ocr));
        assertEquals(InAppPurchase.OcrScans50.getLegacySkus(), Collections.emptySet());

        assertEquals(InAppPurchase.OcrScans10.getSku(), "ocr_purchase_10");
        assertEquals(InAppPurchase.OcrScans10.getType(), ConsumablePurchase.class);
        assertEquals(InAppPurchase.OcrScans10.getProductType(), "inapp");
        assertEquals(InAppPurchase.OcrScans10.getPurchaseFamilies(), Collections.singleton(PurchaseFamily.Ocr));
        assertEquals(InAppPurchase.OcrScans10.getLegacySkus(), Collections.emptySet());
    }

    @Test
    public void from() {
        assertEquals(InAppPurchase.SmartReceiptsPlus, InAppPurchase.from("plus_sku_5"));
        assertEquals(InAppPurchase.SmartReceiptsPlus, InAppPurchase.from("plus_sku_4"));
        assertEquals(InAppPurchase.SmartReceiptsPlus, InAppPurchase.from("pro_sku_3"));
        assertEquals(InAppPurchase.OcrScans50, InAppPurchase.from("ocr_purchase_1"));
        assertEquals(InAppPurchase.OcrScans10, InAppPurchase.from("ocr_purchase_10"));

        // Test validation
        assertEquals(InAppPurchase.TestSubscription, InAppPurchase.from("test_subscription"));
        assertEquals(InAppPurchase.TestSubscription, InAppPurchase.from("test_legacy_subscription"));
    }

    @Test
    public void getConsumablePurchaseSkus() {
        final List<String> purchases = Arrays.asList("ocr_purchase_10", "ocr_purchase_1");
        assertEquals(InAppPurchase.getConsumablePurchaseSkus(), purchases);
    }

    @Test
    public void getSubscriptionSkus() {
        final List<String> purchases = Collections.singletonList("plus_sku_5");
        assertEquals(InAppPurchase.getSubscriptionSkus(), purchases);
    }

}
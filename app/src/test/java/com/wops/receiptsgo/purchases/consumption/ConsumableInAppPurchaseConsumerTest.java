package com.wops.receiptsgo.purchases.consumption;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import com.wops.receiptsgo.purchases.PurchaseManager;
import com.wops.receiptsgo.purchases.model.ConsumablePurchase;
import com.wops.receiptsgo.purchases.model.PurchaseFamily;
import io.reactivex.Completable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ConsumableInAppPurchaseConsumerTest {

    @InjectMocks
    ConsumableInAppPurchaseConsumer consumableInAppPurchaseConsumer;

    @Mock
    PurchaseManager purchaseManager;

    @Mock
    ConsumablePurchase managedProduct;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isConsumed() {
        assertFalse(consumableInAppPurchaseConsumer.isConsumed(managedProduct, PurchaseFamily.Ocr));
    }

    @Test
    public void consumePurchase() {
        final Completable completable = Completable.complete();
        when(purchaseManager.consumePurchase(managedProduct)).thenReturn(completable);
        assertEquals(completable, consumableInAppPurchaseConsumer.consumePurchase(managedProduct, PurchaseFamily.Ocr));
    }


}
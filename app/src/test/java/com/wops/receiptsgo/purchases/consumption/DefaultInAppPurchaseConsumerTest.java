package com.wops.receiptsgo.purchases.consumption;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import com.wops.receiptsgo.purchases.model.ConsumablePurchase;
import com.wops.receiptsgo.purchases.model.PurchaseFamily;
import com.wops.receiptsgo.purchases.model.Subscription;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class DefaultInAppPurchaseConsumerTest {

    @InjectMocks
    DefaultInAppPurchaseConsumer defaultInAppPurchaseConsumer;
    
    @Mock
    ConsumableInAppPurchaseConsumer consumableInAppPurchaseConsumer;
    
    @Mock
    SubscriptionInAppPurchaseConsumer subscriptionInAppPurchaseConsumer;

    @Mock
    ConsumablePurchase consumablePurchase;

    @Mock
    Subscription subscription;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isConsumedForConsumablePurchase() {
        defaultInAppPurchaseConsumer.isConsumed(consumablePurchase, PurchaseFamily.Ocr);
        verify(consumableInAppPurchaseConsumer).isConsumed(consumablePurchase, PurchaseFamily.Ocr);
        verifyZeroInteractions(subscriptionInAppPurchaseConsumer);
    }

    @Test
    public void isConsumedForSubscription() {
        defaultInAppPurchaseConsumer.isConsumed(subscription, PurchaseFamily.Ocr);
        verify(subscriptionInAppPurchaseConsumer).isConsumed(subscription, PurchaseFamily.Ocr);
        verifyZeroInteractions(consumableInAppPurchaseConsumer);
    }

    @Test
    public void consumePurchaseForConsumablePurchase() {
        defaultInAppPurchaseConsumer.consumePurchase(consumablePurchase, PurchaseFamily.Ocr);
        verify(consumableInAppPurchaseConsumer).consumePurchase(consumablePurchase, PurchaseFamily.Ocr);
        verifyZeroInteractions(subscriptionInAppPurchaseConsumer);
    }

    @Test
    public void consumePurchaseForSubscription() {
        defaultInAppPurchaseConsumer.consumePurchase(subscription, PurchaseFamily.Ocr);
        verify(subscriptionInAppPurchaseConsumer).consumePurchase(subscription, PurchaseFamily.Ocr);
        verifyZeroInteractions(consumableInAppPurchaseConsumer);
    }

}
package co.smartreceipts.android.purchases.consumption;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.PurchaseFamily;
import co.smartreceipts.android.purchases.model.Subscription;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SubscriptionInAppPurchaseConsumerTest {

    // Class under test
    SubscriptionInAppPurchaseConsumer subscriptionInAppPurchaseConsumer;

    SharedPreferences preferences;

    @Mock
    Subscription subscription;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(subscription.getInAppPurchase()).thenReturn(InAppPurchase.SmartReceiptsPlus);

        preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        subscriptionInAppPurchaseConsumer = new SubscriptionInAppPurchaseConsumer(preferences);
    }

    @After
    public void tearDown() {
        preferences.edit().clear().apply();
    }

    @Test
    public void isConsumedReturnsFalseForEmpty() {
        assertFalse(subscriptionInAppPurchaseConsumer.isConsumed(subscription, PurchaseFamily.Ocr));
    }

    @Test
    public void consumePurchaseAndThenCheckIsConsumed() {
        subscriptionInAppPurchaseConsumer.consumePurchase(subscription, PurchaseFamily.Ocr)
                .test()
                .assertNoErrors()
                .assertComplete();

        assertTrue(subscriptionInAppPurchaseConsumer.isConsumed(subscription, PurchaseFamily.Ocr));
    }

}
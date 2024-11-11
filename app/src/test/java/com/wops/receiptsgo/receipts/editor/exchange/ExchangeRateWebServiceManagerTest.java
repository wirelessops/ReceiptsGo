package com.wops.receiptsgo.receipts.editor.exchange;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.sql.Date;
import java.util.Locale;
import java.util.TimeZone;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.Events;
import com.wops.receiptsgo.apis.ExchangeRateService;
import com.wops.receiptsgo.config.ConfigurationManager;
import com.wops.receiptsgo.model.factory.ExchangeRateBuilderFactory;
import com.wops.receiptsgo.model.gson.ExchangeRate;
import com.wops.receiptsgo.purchases.PurchaseManager;
import com.wops.receiptsgo.purchases.model.InAppPurchase;
import com.wops.receiptsgo.purchases.source.PurchaseSource;
import com.wops.receiptsgo.purchases.wallet.PurchaseWallet;
import com.wops.receiptsgo.utils.ConfigurableResourceFeature;
import com.wops.receiptsgo.utils.TestLocaleToggler;
import com.wops.receiptsgo.utils.TestTimezoneToggler;
import com.wops.receiptsgo.widget.model.UiIndicator;
import io.reactivex.Observable;

@SuppressWarnings("unchecked")
@RunWith(RobolectricTestRunner.class)
public class ExchangeRateWebServiceManagerTest {

    private static final Date DATE = new Date(1482022800000L);
    private static final String BASE_CURRENCY = "EUR";
    private static final String QUOTE_CURRENCY = "USD";
    private static final String APP_ID = "app_id";

    // Class under test
    ExchangeRateServiceManager exchangeRateServiceManager;

    @Mock
    Context context;

    @Mock
    PurchaseManager purchaseManager;

    @Mock
    PurchaseWallet purchaseWallet;

    @Mock
    Analytics analytics;

    @Mock
    ExchangeRateService exchangeRateService;

    @Mock
    ConfigurationManager configurationManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TestLocaleToggler.setDefaultLocale(Locale.US);
        TestTimezoneToggler.setDefaultTimeZone(TimeZone.getTimeZone("America/New_York"));

        when(context.getApplicationContext()).thenReturn(context);
        when(context.getString(anyInt())).thenReturn(APP_ID);
        exchangeRateServiceManager = new ExchangeRateServiceManager(context, purchaseManager, purchaseWallet, analytics, configurationManager, exchangeRateService);
    }

    @After
    public void tearDown() throws Exception {
        TestLocaleToggler.resetDefaultLocale();
        TestTimezoneToggler.resetDefaultTimeZone();
    }

    @Test
    public void getExchangeRateInitiatesPurchaseWithoutSubscription() {
        when(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(false);
        exchangeRateServiceManager.getExchangeRateOrInitiatePurchase(DATE, BASE_CURRENCY, QUOTE_CURRENCY)
                .test()
                .assertNoValues()
                .assertNoErrors();
        verify(purchaseManager).initiatePurchase(InAppPurchase.SmartReceiptsPlus, PurchaseSource.ExchangeRate);
        verify(exchangeRateService, never()).getExchangeRate(DATE, BASE_CURRENCY, QUOTE_CURRENCY);
        verifyZeroInteractions(analytics);
    }

    @Test
    public void getExchangeRateInitiatesPurchaseWithoutNewSubscriptionPlan() {
        when(configurationManager.isEnabled(ConfigurableResourceFeature.SubscriptionModel)).thenReturn(true);
        when(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(false);
        when(purchaseWallet.hasActivePurchase(InAppPurchase.PremiumSubscriptionPlan)).thenReturn(false);
        exchangeRateServiceManager.getExchangeRateOrInitiatePurchase(DATE, BASE_CURRENCY, QUOTE_CURRENCY)
                .test()
                .assertNoValues()
                .assertNoErrors();
        verify(purchaseManager).initiatePurchase(InAppPurchase.PremiumSubscriptionPlan, PurchaseSource.ExchangeRate);
        verify(exchangeRateService, never()).getExchangeRate(DATE, BASE_CURRENCY, QUOTE_CURRENCY);
        verifyZeroInteractions(analytics);
    }

    @Test
    public void getExchangeRateInitiatesPurchaseWithSubscriptionButReturnsError() {
        when(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(true);
        when(exchangeRateService.getExchangeRate(DATE, APP_ID, BASE_CURRENCY)).thenReturn(Observable.error(new Exception("test")));
        exchangeRateServiceManager.getExchangeRateOrInitiatePurchase(DATE, BASE_CURRENCY, QUOTE_CURRENCY)
                .test()
                .assertValues(UiIndicator.loading(), UiIndicator.error())
                .assertNoErrors();
        verify(purchaseManager, never()).initiatePurchase(InAppPurchase.SmartReceiptsPlus, PurchaseSource.ExchangeRate);
        verify(analytics).record(Events.Receipts.RequestExchangeRate);
        verify(analytics).record(Events.Receipts.RequestExchangeRateFailed);
        verifyZeroInteractions(analytics);
    }

    @Test
    public void getExchangeRateInitiatesPurchaseWithSubscriptionButReturnsInvalidRate() {
        final ExchangeRateBuilderFactory builder = new ExchangeRateBuilderFactory();
        builder.setBaseCurrency(BASE_CURRENCY);
        final ExchangeRate exchangeRate = builder.build();
        when(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(true);
        when(exchangeRateService.getExchangeRate(DATE, APP_ID, BASE_CURRENCY)).thenReturn(Observable.just(exchangeRate));
        exchangeRateServiceManager.getExchangeRateOrInitiatePurchase(DATE, BASE_CURRENCY, QUOTE_CURRENCY)
                .test()
                .assertValues(UiIndicator.loading(), UiIndicator.error())
                .assertNoErrors();
        verify(purchaseManager, never()).initiatePurchase(InAppPurchase.SmartReceiptsPlus, PurchaseSource.ExchangeRate);
        verify(analytics).record(Events.Receipts.RequestExchangeRate);
        verify(analytics).record(Events.Receipts.RequestExchangeRateFailedMissingQuoteCurrency);
        verifyZeroInteractions(analytics);
    }

    @Test
    public void getExchangeRateInitiatesPurchaseWithSubscriptionAndReturnsValidRate() {
        final ExchangeRateBuilderFactory builder = new ExchangeRateBuilderFactory();
        builder.setBaseCurrency(BASE_CURRENCY);
        builder.setRate(QUOTE_CURRENCY, 1.00);
        final ExchangeRate exchangeRate = builder.build();
        when(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(true);
        when(exchangeRateService.getExchangeRate(DATE, APP_ID, BASE_CURRENCY)).thenReturn(Observable.just(exchangeRate));
        exchangeRateServiceManager.getExchangeRateOrInitiatePurchase(DATE, BASE_CURRENCY, QUOTE_CURRENCY)
                .test()
                .assertValues(UiIndicator.loading(), UiIndicator.success(exchangeRate))
                .assertNoErrors();
        verify(purchaseManager, never()).initiatePurchase(InAppPurchase.SmartReceiptsPlus, PurchaseSource.ExchangeRate);
        verify(analytics).record(Events.Receipts.RequestExchangeRate);
        verify(analytics).record(Events.Receipts.RequestExchangeRateSuccess);
        verifyZeroInteractions(analytics);
    }
}
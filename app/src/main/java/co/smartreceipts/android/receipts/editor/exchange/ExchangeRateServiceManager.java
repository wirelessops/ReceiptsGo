package co.smartreceipts.android.receipts.editor.exchange;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.gson.GsonBuilder;

import java.sql.Date;
import java.util.Calendar;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.Events;
import co.smartreceipts.android.apis.ApiValidationException;
import co.smartreceipts.android.apis.ExchangeRateService;
import co.smartreceipts.android.apis.SmartReceiptsApisRxJavaCallAdapterFactory;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.widget.model.UiIndicator;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * This class maintains responsibility for fetching the exchange rate from the
 */
@ApplicationScope
public class ExchangeRateServiceManager {

    private final Context context;
    private final PurchaseManager purchaseManager;
    private final PurchaseWallet purchaseWallet;
    private final Analytics analytics;
    private final ExchangeRateService exchangeRateService;

    @Inject
    public ExchangeRateServiceManager(@NonNull Context context,
                                      @NonNull PurchaseManager purchaseManager,
                                      @NonNull PurchaseWallet purchaseWallet,
                                      @NonNull Analytics analytics) {
        this(context, purchaseManager, purchaseWallet, analytics, new Retrofit.Builder()
                .baseUrl("https://openexchangerates.org")
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setDateFormat("yyyy-MM-dd").create()))
                .addCallAdapterFactory(SmartReceiptsApisRxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(ExchangeRateService.class));
    }

    public ExchangeRateServiceManager(@NonNull Context context,
                                      @NonNull PurchaseManager purchaseManager,
                                      @NonNull PurchaseWallet purchaseWallet,
                                      @NonNull Analytics analytics,
                                      @NonNull ExchangeRateService exchangeRateService) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.purchaseManager = Preconditions.checkNotNull(purchaseManager);
        this.purchaseWallet = Preconditions.checkNotNull(purchaseWallet);
        this.exchangeRateService = Preconditions.checkNotNull(exchangeRateService);
        this.analytics = Preconditions.checkNotNull(analytics);
    }

    /**
     * Submits a network request to get the exchange rate for a particular currency on a given date. In an example of
     * EUR/USD, EUR would refer to the base currency code (ie receipt one), and USD would refer to the quote currency
     * code (ie trip one)
     *
     * @param date the desired {@link Date} to get the currency for. If this date is in the future, it will fetch
     *             for the current one
     * @param baseCurrencyCode the base currency code
     * @param quoteCurrencyCode the quote currency code
     * @return an {@link Observable} that will emit a {@link UiIndicator}, which may contain a {@link ExchangeRate}
     * set of data that supports the base -> quote conversion
     */
    @NonNull
    public Observable<UiIndicator<ExchangeRate>> getExchangeRate(@NonNull Date date, @NonNull String baseCurrencyCode, @NonNull String quoteCurrencyCode) {
        return Observable.just(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus))
                .filter(hasPlusSubscription -> hasPlusSubscription)
                .map(ignored -> {
                    final Date today = new Date(Calendar.getInstance().getTimeInMillis());
                    if (date.after(today)) {
                        return today;
                    } else {
                        return date;
                    }
                })
                .flatMap(dateAsTodayOrEarlier ->
                        exchangeRateService.getExchangeRate(dateAsTodayOrEarlier, context.getString(R.string.exchange_rate_key), baseCurrencyCode)
                        .doOnSubscribe(ignored -> {
                            Logger.info(ExchangeRateServiceManager.this, "Fetching the exchange rate for {} on {}", baseCurrencyCode, dateAsTodayOrEarlier);
                            analytics.record(Events.Receipts.RequestExchangeRate);
                        })
                        .flatMap(exchangeRate -> {
                            if (exchangeRate.supportsExchangeRateFor(quoteCurrencyCode)) {
                                return Observable.just(exchangeRate);
                            } else {
                                return Observable.error(new ApiValidationException("The API response failed to include our quote currency: " + quoteCurrencyCode));
                            }
                        })
                        .doOnError(throwable -> {
                            Logger.error(ExchangeRateServiceManager.this, "Failed to fetch the exchange for " + baseCurrencyCode, throwable);
                            if (throwable instanceof ApiValidationException) {
                                analytics.record(Events.Receipts.RequestExchangeRateFailedMissingQuoteCurrency);
                            } else {
                                analytics.record(Events.Receipts.RequestExchangeRateFailed);
                            }
                        })
                        .doOnNext(exchangeRate -> {
                            Logger.info(ExchangeRateServiceManager.this, "Successfully fetched the exchange rate for {} on {}", baseCurrencyCode, dateAsTodayOrEarlier);
                            analytics.record(Events.Receipts.RequestExchangeRateSuccess);
                        })
                        .map(UiIndicator::success)
                        .onErrorReturn(ignore -> UiIndicator.error())
                        .startWith(UiIndicator.loading())
                );
    }

    /**
     * <p>
     * A method that will first check if the user has a valid {@link InAppPurchase#SmartReceiptsPlus} subscription. If not,
     * it will initiate the purchase of this item. If it already has this purchase, it will fetch the exchange rate via
     * {@link #getExchangeRate(Date, String, String)}.
     * </p>
     *
     * @param date the desired {@link Date} to get the currency for. If this date is in the future, it will fetch
     *             for the current one
     * @param baseCurrencyCode the base currency code
     * @param quoteCurrencyCode the quote currency code
     * @return an {@link Observable} that will emit a {@link UiIndicator}, which may contain a {@link ExchangeRate}
     * set of data that supports the base -> quote conversion
     */
    @NonNull
    public Observable<UiIndicator<ExchangeRate>> getExchangeRateOrInitiatePurchase(@NonNull Date date, @NonNull String baseCurrencyCode, @NonNull String quoteCurrencyCode) {
        return Observable.just(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus))
                .doOnNext(hasPlusSubscription -> {
                    if (!hasPlusSubscription) {
                        Logger.info(this, "Attempting to retry without valid subscription. Directing user to purchase intent");
                        purchaseManager.initiatePurchase(InAppPurchase.SmartReceiptsPlus, PurchaseSource.ExchangeRate);
                    }
                })
                .flatMap(hasPlusSubscription -> getExchangeRate(date, baseCurrencyCode, quoteCurrencyCode));
    }
}

package com.wops.receiptsgo.ocr.widget.configuration

import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.DataPoint
import co.smartreceipts.analytics.events.DefaultDataPointEvent
import co.smartreceipts.analytics.events.Events
import co.smartreceipts.analytics.log.Logger
import com.wops.receiptsgo.config.ConfigurationManager
import com.wops.receiptsgo.ocr.purchases.OcrPurchaseTracker
import com.wops.receiptsgo.purchases.PurchaseManager
import com.wops.receiptsgo.purchases.model.ConsumablePurchase
import com.wops.receiptsgo.purchases.model.InAppPurchase
import com.wops.receiptsgo.purchases.model.PurchaseFamily
import com.wops.receiptsgo.purchases.source.PurchaseSource
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.wops.receiptsgo.utils.ConfigurableResourceFeature
import com.wops.receiptsgo.utils.rx.RxSchedulers
import co.smartreceipts.core.di.scopes.FragmentScope
import co.smartreceipts.core.identity.IdentityManager
import co.smartreceipts.core.identity.store.EmailAddress
import com.android.billingclient.api.ProductDetails
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Named

@FragmentScope
class OcrConfigurationInteractor @Inject constructor(
    private val identityManager: IdentityManager,
    private val ocrPurchaseTracker: OcrPurchaseTracker,
    private val purchaseManager: PurchaseManager,
    private val userPreferenceManager: UserPreferenceManager,
    private val configurationManager: ConfigurationManager,
    private val analytics: Analytics,
    @Named(RxSchedulers.MAIN)
    private val observeOnScheduler: Scheduler
) {

    val email: EmailAddress?
        get() = identityManager.email

    val isUserLoggedIn: Boolean
        get() = identityManager.isLoggedIn


    fun getRemainingScansStream(): Observable<Int> {
        return ocrPurchaseTracker.remainingScansStream
            .observeOn(observeOnScheduler)
    }

    fun getAvailableOcrPurchases(): Single<List<ProductDetails>> {
        return purchaseManager.allAvailablePurchaseSkus
            .map { set ->
                set.filter { skuDetails ->
                    val inAppPurchase = InAppPurchase.from(skuDetails.productId)
                    inAppPurchase != null && inAppPurchase.type == ConsumablePurchase::class.java
                            && inAppPurchase.purchaseFamilies.contains(PurchaseFamily.Ocr)
                }
                    .sortedWith(Comparator { purchase1, purchase2 ->
                        BigDecimal.valueOf(purchase1.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0)
                            .compareTo(
                                BigDecimal.valueOf(
                                    purchase2.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0
                                )
                            )
                    })
            }
            .observeOn(observeOnScheduler)
    }

    fun getOcrIsEnabled(): Observable<Boolean> = userPreferenceManager.getObservable(UserPreference.Misc.OcrIsEnabled)

    val isSubscriptionsEnabled: Boolean
        get() = configurationManager.isEnabled(ConfigurableResourceFeature.SubscriptionModel)

    fun getAllowUsToSaveImagesRemotely(): Observable<Boolean> {
        return userPreferenceManager.getObservable(UserPreference.Misc.OcrIncognitoMode)
            .map { incognito -> !incognito }
    }

    fun startOcrPurchase(inAppPurchase: InAppPurchase?) {
        if (inAppPurchase != null) {
            analytics.record(
                DefaultDataPointEvent(Events.Ocr.OcrPurchaseClicked).addDataPoint(
                    DataPoint("sku", inAppPurchase)
                )
            )
            purchaseManager.initiatePurchase(inAppPurchase, PurchaseSource.Ocr)
        } else {
            Logger.error(this, "Unexpected state in which the in app purchase is null")
        }
    }

    fun startOcrPurchase(skuDetails: ProductDetails) {
        analytics.record(
            DefaultDataPointEvent(Events.Ocr.OcrPurchaseClicked).addDataPoint(
                DataPoint(
                    "sku",
                    skuDetails
                )
            )
        )
        purchaseManager.initiatePurchase(skuDetails, PurchaseSource.Ocr)
    }

    fun logOut() = identityManager.logOut()

    fun setOcrIsEnabled(ocrIsEnabled: Boolean) {
        analytics.record(
            DefaultDataPointEvent(Events.Ocr.OcrIsEnabledToggled).addDataPoint(
                DataPoint("value", ocrIsEnabled)
            )
        )
        userPreferenceManager[UserPreference.Misc.OcrIsEnabled] = ocrIsEnabled
    }

    fun setAllowUsToSaveImagesRemotely(saveImagesRemotely: Boolean) {
        analytics.record(
            DefaultDataPointEvent(Events.Ocr.OcrIncognitoModeToggled).addDataPoint(
                DataPoint("value", !saveImagesRemotely)
            )
        )
        userPreferenceManager[UserPreference.Misc.OcrIncognitoMode] = !saveImagesRemotely
    }

}

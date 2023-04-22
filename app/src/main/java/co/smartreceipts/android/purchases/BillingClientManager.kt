package co.smartreceipts.android.purchases

import android.app.Activity
import android.content.Context
import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.purchases.model.*
import co.smartreceipts.android.purchases.source.PurchaseSource
import co.smartreceipts.android.purchases.subscriptions.RemoteSubscriptionManager
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import co.smartreceipts.android.subscriptions.SubscriptionUploader
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.android.billingclient.api.*
import com.google.common.base.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@ApplicationScope
class BillingClientManager @Inject constructor(
    context: Context,
    remoteSubscriptionManager: RemoteSubscriptionManager,
    private val purchaseWallet: PurchaseWallet,
    private val subscriptionUploader: SubscriptionUploader
) {

    private companion object {
        val LIST_OF_SUBS_SKUS = InAppPurchase.subscriptionSkus
        val LIST_OF_IN_APP_SKUS = InAppPurchase.consumablePurchaseSkus
    }

    private val billingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(::handlePurchases)
        .enablePendingPurchases()
        .build()

    private val isConnectedSubject = BehaviorSubject.createDefault<Boolean>(false)

    private val billingClientStateListener = object : BillingClientStateListener {
        override fun onBillingServiceDisconnected() {
            isConnectedSubject.onNext(false)
        }

        override fun onBillingSetupFinished(result: BillingResult) {
            val connected = result.responseCode == BillingClient.BillingResponseCode.OK
            Logger.debug(this, "onBillingSetupFinished, code ${result.responseCode}")

            isConnectedSubject.onNext(connected)
        }
    }

    private val listeners: CopyOnWriteArrayList<PurchaseEventsListener> = CopyOnWriteArrayList()

    @Volatile
    private var lastPurchaseSource: PurchaseSource? = null

    init {
        // Pre-load all of our purchases into memory
        Observable.fromCallable { purchaseWallet.activeLocalInAppPurchases }
            .subscribeOn(Schedulers.io())
            .subscribe()

        // Fetch all of our remote subscriptions and notify if appropriate
        remoteSubscriptionManager.getNewRemoteSubscriptions()
            .subscribe({ newInAppSubscriptions ->
                newInAppSubscriptions.forEach { (_, inAppPurchase) ->
                    listeners.forEach { it.onPurchaseSuccess(inAppPurchase, PurchaseSource.Remote) }
                }
            }) { Logger.error(this, "Failed to fetch our remote subscriptions") }

        queryAllOwnedPurchasesAndSync()
            .subscribe({ purchases -> Logger.debug(this, "owned purchases: $purchases") },
                { t -> Logger.error(this, t) })
    }

    fun addPurchaseEventListener(listener: PurchaseEventsListener) = listeners.add(listener)

    fun removePurchaseEventListener(listener: PurchaseEventsListener) = listeners.remove(listener)

    fun consumePurchase(purchase: ConsumablePurchase): Completable {
        return billingClient.consumePurchase(purchase.purchaseToken)
    }

    fun querySkuDetails(purchase: InAppPurchase): Single<SkuDetails> {
        val skuList = mutableListOf(purchase.sku)

        val skuType: String = when (purchase.type) {
            ConsumablePurchase::class.java -> BillingClient.SkuType.INAPP
            Subscription::class.java -> BillingClient.SkuType.SUBS
            else -> throw IllegalStateException("Purchase type is unknown")
        }

        return billingClient.querySkuDetailsAsSingle(skuType, skuList)
            .map { it.first() }
            .ensureConnection()
    }

    fun initiatePurchase(skuDetails: SkuDetails, activity: Activity): Completable {
        Logger.debug(this, "Initiating purchase ${skuDetails.sku}")

        return Single.just(skuDetails)
            .flatMap<Optional<ManagedProduct>> { desiredSkuDetails ->
                val desiredPurchase = InAppPurchase.from(desiredSkuDetails.sku)!!

                if (desiredPurchase.purchaseFamilies.contains(PurchaseFamily.SubscriptionPlans)) {
                    // if this is a subscription plan - we need to check if we need to handle subscription upgrade/downgrade
                    return@flatMap queryOwnedSubscriptions()
                        .map { ownedPlan ->
                            val previousPlan = ownedPlan.firstOrNull { managedProduct ->
                                managedProduct.inAppPurchase.purchaseFamilies.contains(
                                    PurchaseFamily.SubscriptionPlans
                                )
                            }
                            Logger.debug(this, "Found owned plan: ${previousPlan?.inAppPurchase?.sku}")
                            return@map Optional.fromNullable(previousPlan)
                        }
                } else {
                    // no need to handle subscription upgrade/downgrade
                    return@flatMap Single.just(Optional.absent())
                }
            }
            .flatMapCompletable { previousPlan ->
                val purchaseParamsBuilder = BillingFlowParams.newBuilder()

                if (previousPlan.isPresent) {
                    purchaseParamsBuilder.setSubscriptionUpdateParams(
                        BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                            .setOldSkuPurchaseToken(previousPlan.get().purchaseToken)
                            .build()
                    )
                }

                val purchaseParams = purchaseParamsBuilder.setSkuDetails(skuDetails).build()

                val billingResult = billingClient.launchBillingFlow(activity, purchaseParams)

                val responseCode = billingResult.responseCode
                val debugMessage = billingResult.debugMessage
                Logger.debug(this, "launchBillingFlow: BillingResponse $responseCode $debugMessage")

                when (responseCode) {
                    BillingClient.BillingResponseCode.OK -> Completable.complete()
                    else -> Completable.error(BillingClientException(responseCode, debugMessage))
                }
            }
    }

    fun queryAllAvailablePurchases(): Single<Set<SkuDetails>> {
        return Single.zip(
            querySubscriptionsSkuDetails(),
            queryInAppSkuDetails(),
            BiFunction { subscriptionSkus, inAppSkus -> subscriptionSkus + inAppSkus }
        )
    }

    fun queryAllOwnedPurchasesAndSync(): Single<Set<ManagedProduct>> {
        Logger.debug(this, "queryAllOwnedPurchasesAndSync")
        return Single.zip(queryOwnedInAppPurchases(), queryOwnedSubscriptions(),
            BiFunction<Set<ManagedProduct>, Set<ManagedProduct>, Set<ManagedProduct>> { consumablePurchases, subscriptions ->
                consumablePurchases + subscriptions
            })
            .map { purchasedProducts ->
                Logger.debug(this, purchasedProducts.toString())
                purchaseWallet.updateLocalInAppPurchasesInWallet(purchasedProducts)
                purchaseWallet.activeLocalInAppPurchases
            }
    }


    fun acknowledgePurchase(purchase: Purchase): Completable {
        Logger.debug(this, "acknowledgePurchase")

        if (purchase.isAcknowledged) {
            Logger.info(this, "Trying to acknowledge acknowledged purchase")
            return Completable.complete()
        }

        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        return Completable.fromCallable {
            billingClient.acknowledgePurchase(acknowledgeParams) { billingResult: BillingResult ->
                Logger.info(this, "Acknowledge result: ${billingResult.responseCode}")
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    throw Exception("Failed to acknowledge billingResult: $billingResult")
                }
            }
        }
    }

    private fun querySubscriptionsSkuDetails(): Single<Set<SkuDetails>> {
        return billingClient.querySkuDetailsAsSingle(BillingClient.SkuType.SUBS, LIST_OF_SUBS_SKUS)
            .doOnSuccess { Logger.info(this, "Available subscriptions: $it") }
            .ensureConnection()
    }

    private fun queryInAppSkuDetails(): Single<Set<SkuDetails>> {
        return billingClient.querySkuDetailsAsSingle(
            BillingClient.SkuType.INAPP,
            LIST_OF_IN_APP_SKUS
        )
            .doOnSuccess { Logger.info(this, "Available inapp: ${it.size}") }
            .ensureConnection()
    }

    private fun queryOwnedSubscriptions(): Single<Set<ManagedProduct>> {
        Logger.debug(this, "queryPurchases: SUBS")

        return billingClient.queryPurchasesAsSingle(BillingClient.SkuType.SUBS)
            .map { list -> list.filter { it.isAcknowledged } }
            .map<Set<ManagedProduct>> { purchases ->
                purchases.map {
                    Subscription(
                        InAppPurchase.from(it.skus.first())!!,
                        it.originalJson,
                        it.purchaseToken,
                        it.signature
                    )
                }.toSet()
            }
            .doOnError { t -> Logger.error(this, t) }
            .ensureConnection()
    }

    fun queryUnacknowledgedSubscriptions(): Single<List<Purchase>> {
        return billingClient.queryPurchasesAsSingle(BillingClient.SkuType.SUBS)
            .map { list -> list.filter { !it.isAcknowledged }}
            .doOnError { t -> Logger.error(this, t) }
            .ensureConnection()
    }

    private fun queryOwnedInAppPurchases(): Single<Set<ManagedProduct>> {
        Logger.debug(this, "queryPurchases: INAPP")

        return billingClient.queryPurchasesAsSingle(BillingClient.SkuType.INAPP)
            .map<Set<ManagedProduct>> { purchases ->
                purchases.map {
                    ConsumablePurchase(
                        InAppPurchase.from(it.skus.first())!!,
                        it.originalJson,
                        it.purchaseToken,
                        it.signature
                    )
                }.toSet()
            }
            .doOnError { t -> Logger.error(this, t) }
            .ensureConnection()
    }

    private fun connectBillingClient(): Single<Boolean> {
        val connectionState = billingClient.connectionState

        Logger.debug(
            this,
            "Ensuring connection. Connection state is $connectionState, isReady ${billingClient.isReady}"
        )

        if (billingClient.isReady) {
            return Single.just(true)
        }

        isConnectedSubject.onNext(false)

        if (billingClient.connectionState != BillingClient.ConnectionState.CONNECTING) {
            billingClient.startConnection(billingClientStateListener)
        }

        return isConnectedSubject.filter { it }
            .timeout(3L, TimeUnit.SECONDS)
            .doOnError { Logger.warn(this, "Failed to ensure connection state") }
            .onErrorReturnItem(false)
            .first(false)
    }

    private fun <T> Single<T>.ensureConnection(): Single<T> {
        return connectBillingClient().flatMap {
            when {
                it -> this
                else -> Single.error(Exception("Not connected"))
            }
        }
    }

    /**
     * Called when new purchases are detected.
     */
    private fun handlePurchases(billingResult: BillingResult, purchases: List<Purchase>?) {
        val source: PurchaseSource = lastPurchaseSource ?: PurchaseSource.Unknown
        lastPurchaseSource = null

        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Logger.debug(this, "onPurchasesUpdated: $responseCode $debugMessage")

        if (responseCode != BillingClient.BillingResponseCode.OK) {
            Logger.warn(this, "Unexpected BillingResponseCode: $responseCode")
            listeners.forEach { it.onPurchaseFailed(source) }
            return
        }

        Logger.debug(this, "handlePurchases: ${purchases?.size} purchase(s)")

        purchases?.forEach { purchase ->
            Logger.debug(this, "handlePurchase $purchase")

            if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                Logger.warn(this, "Got purchase in PENDING state, skus: ${purchase.skus}")
                listeners.forEach { it.onPurchasePending() }
                return
            }

            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

                val sku = purchase.skus[0]
                val inAppPurchase = InAppPurchase.from(sku)

                if (inAppPurchase == null) {
                    listeners.forEach { it.onPurchaseFailed(source) }
                    Logger.warn(
                        this, "Retrieved an unknown sku following a successful purchase: $sku"
                    )
                    return
                }

                val managedProduct =
                    ManagedProductFactory(
                        inAppPurchase, purchase.originalJson, purchase.signature
                    ).get()

                // subscriptions must be sent to the server before acknowledge
                if (inAppPurchase.productType == BillingClient.SkuType.SUBS) {
                    subscriptionUploader.uploadSubscription(managedProduct)
                        .retry(3)
                        .andThen(acknowledgePurchase(purchase))
                        .subscribe({
                            purchaseWallet.addLocalInAppPurchaseToWallet(managedProduct)
                            listeners.forEach { it.onPurchaseSuccess(inAppPurchase, source) }
                        },
                            { t ->
                                Logger.error(
                                    this, "Failed to upload subscription and acknowledge it.", t
                                )
                                listeners.forEach { it.onPurchaseFailed(source) }
                            })
                } else {
                    // consumable inApp
                    // Note: INAPP purchases will be consumed immediately, so no need to acknowledge them
                    purchaseWallet.addLocalInAppPurchaseToWallet(managedProduct)
                    listeners.forEach { it.onPurchaseSuccess(inAppPurchase, source) }
                }
            }
        }

    }
}
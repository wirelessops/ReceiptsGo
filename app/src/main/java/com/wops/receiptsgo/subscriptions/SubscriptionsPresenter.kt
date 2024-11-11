package com.wops.receiptsgo.subscriptions

import co.smartreceipts.analytics.log.Logger
import com.wops.receiptsgo.purchases.PurchaseEventsListener
import com.wops.receiptsgo.purchases.model.InAppPurchase
import com.wops.receiptsgo.purchases.source.PurchaseSource
import com.wops.receiptsgo.purchases.subscriptionFormattedPrice
import com.wops.receiptsgo.widget.viper.BaseViperPresenter
import co.smartreceipts.core.di.scopes.ActivityScope
import co.smartreceipts.core.identity.IdentityManager
import javax.inject.Inject

@ActivityScope
class SubscriptionsPresenter @Inject constructor(
    view: SubscriptionsView,
    interactor: SubscriptionsInteractor,
    private val identityManager: IdentityManager
) : BaseViperPresenter<SubscriptionsView, SubscriptionsInteractor>(view, interactor),
    PurchaseEventsListener {

    override fun subscribe() {

        interactor.addSubscriptionListener(this)

        compositeDisposable.add(
            view.cancelSubscriptionInfoClicks
                .subscribe { view.redirectToPlayStoreSubscriptions() }
        )

        compositeDisposable.add(
            view.standardSubscriptionClicks
                .subscribe {
                    when {
                        identityManager.isLoggedIn -> {
                            view.presentLoading()
                            interactor.purchaseStandardPlan()
                        }

                        else -> view.navigateToLogin()
                    }
                }
        )

        compositeDisposable.add(
            view.premiumSubscriptionClicks
                .subscribe {
                    when {
                        identityManager.isLoggedIn -> {
                            view.presentLoading()
                            interactor.purchasePremiumPlan()
                        }

                        else -> view.navigateToLogin()
                    }
                }
        )

        compositeDisposable.add(
            interactor.getPlansWithOwnership()
                .subscribe({ plans ->
                    var userOwnsPlan = false
                    for (plan in plans) {
                        if (plan.key.productId == InAppPurchase.StandardSubscriptionPlan.sku) {
                            val isOwned = plan.value
                            view.presentStandardPlan(if (isOwned) null else plan.key.subscriptionFormattedPrice?.formatPrice())
                            userOwnsPlan = userOwnsPlan || isOwned
                        } else if (plan.key.productId == InAppPurchase.PremiumSubscriptionPlan.sku) {
                            val isOwned = plan.value
                            view.presentPremiumPlan(if (isOwned) null else plan.key.subscriptionFormattedPrice?.formatPrice())
                            userOwnsPlan = userOwnsPlan || isOwned
                        }
                    }

                    view.presentCancelInfo(userOwnsPlan)
                }, { t -> Logger.error(this, t) })
        )
    }

    override fun unsubscribe() {
        interactor.removeSubscriptionListener(this)
        super.unsubscribe()
    }

    override fun onPurchaseSuccess(inAppPurchase: InAppPurchase, purchaseSource: PurchaseSource) {
        view.presentSuccessSubscription()
    }

    override fun onPurchaseFailed(purchaseSource: PurchaseSource) {
        view.presentFailedSubscription()
    }

    override fun onPurchasePending() {
        /* no-op */
    }

    private fun String.formatPrice(): String {
        return this.replace(".00", "").replace(",00", "")
    }
}
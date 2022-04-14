package co.smartreceipts.android.subscriptions

import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.purchases.PurchaseEventsListener
import co.smartreceipts.android.purchases.model.InAppPurchase
import co.smartreceipts.android.purchases.source.PurchaseSource
import co.smartreceipts.android.widget.viper.BaseViperPresenter
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

        // TODO: 07.12.2021 add analytics

        compositeDisposable.add(
            view.cancelSubscriptionInfoClicks
                .subscribe { view.redirectToPlayStoreSubscriptions() }
        )

        compositeDisposable.add(
            view.standardSubscriptionClicks
                .subscribe {
                    when {
                        identityManager.isLoggedIn -> interactor.purchaseStandardPlan()
                        else -> view.navigateToLogin()
                    }
                }
        )

        compositeDisposable.add(
            view.premiumSubscriptionClicks
                .subscribe {
                    when {
                        identityManager.isLoggedIn -> interactor.purchasePremiumPlan()
                        else -> view.navigateToLogin()
                    }
                }
        )

        compositeDisposable.add(
            interactor.getPlansWithOwnership()
                .subscribe({ plans ->
                    var userOwnsPlan: Boolean = false
                    for (plan in plans) {
                        if (plan.key.sku == InAppPurchase.StandardSubscriptionPlan.sku) {
                            val isOwned = plan.value
                            view.presentStandardPlan(if (isOwned) null else plan.key.price)
                            userOwnsPlan = userOwnsPlan || isOwned
                        } else if (plan.key.sku == InAppPurchase.PremiumSubscriptionPlan.sku) {
                            val isOwned = plan.value
                            view.presentPremiumPlan(if (isOwned) null else plan.key.price)
                            userOwnsPlan = userOwnsPlan || isOwned
                        }
                    }

                    view.presentCancelInfo(userOwnsPlan)
                }, { t -> Logger.error(this, t)})
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
}
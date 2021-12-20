package co.smartreceipts.android.subscriptions

import co.smartreceipts.android.purchases.PurchaseEventsListener
import co.smartreceipts.android.purchases.model.InAppPurchase
import co.smartreceipts.android.purchases.source.PurchaseSource
import co.smartreceipts.android.widget.viper.BaseViperPresenter
import co.smartreceipts.core.di.scopes.ActivityScope
import javax.inject.Inject

@ActivityScope
class SubscriptionsPresenter @Inject constructor(
    view: SubscriptionsView,
    interactor: SubscriptionsInteractor
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
                .subscribe { interactor.purchaseStandardPlan() }
        )

        compositeDisposable.add(
            view.premiumSubscriptionClicks
                .subscribe { interactor.purchasePremiumPlan() }
        )

        compositeDisposable.add(
            interactor.getPlansWithOwnership()
                .subscribe({ plans ->
                    for (plan in plans) {
                        if (plan.key.sku == InAppPurchase.StandardSubscriptionPlan.sku) {
                            val isOwned = plan.value
                            view.presentStandardPlan(if (isOwned) null else plan.key.price)
                        } else if (plan.key.sku == InAppPurchase.PremiumSubscriptionPlan.sku) {
                            val isOwned = plan.value
                            view.presentPremiumPlan(if (isOwned) null else plan.key.price)
                        }
                    }
                }, { t -> })
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
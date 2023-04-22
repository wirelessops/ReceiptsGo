package co.smartreceipts.android.subscriptions

import io.reactivex.Observable

interface SubscriptionsView {

    val standardSubscriptionClicks: Observable<Unit>

    val premiumSubscriptionClicks: Observable<Unit>

    val cancelSubscriptionInfoClicks: Observable<Unit>


    fun presentStandardPlan(price: String?)

    fun presentPremiumPlan(price: String?)

    fun presentCancelInfo(isVisible: Boolean)

    fun redirectToPlayStoreSubscriptions()

    fun presentSuccessSubscription()

    fun presentFailedSubscription()

    fun presentLoading()

    fun navigateToLogin()

    fun navigateBack()
}
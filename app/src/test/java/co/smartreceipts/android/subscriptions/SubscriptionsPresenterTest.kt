package co.smartreceipts.android.subscriptions

import co.smartreceipts.android.purchases.model.InAppPurchase
import co.smartreceipts.core.identity.IdentityManager
import com.android.billingclient.api.SkuDetails
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SubscriptionsPresenterTest {

    // Class under test
    private lateinit var presenter: SubscriptionsPresenter

    private val view = mock<SubscriptionsView>()
    private val interactor = mock<SubscriptionsInteractor>()
    private val identityManager = mock<IdentityManager>()

    private val standardSkuDetails = mock<SkuDetails>()
    private val premiumSkuDetails = mock<SkuDetails>()

    companion object {
        const val STANDARD_PRICE = "50"
        const val PREMIUM_PRICE = "100"
    }

    @Before
    fun setUp() {
        whenever(view.standardSubscriptionClicks).thenReturn(Observable.never())
        whenever(view.premiumSubscriptionClicks).thenReturn(Observable.never())
        whenever(view.cancelSubscriptionInfoClicks).thenReturn(Observable.never())

        whenever(identityManager.isLoggedIn).thenReturn(false)

        whenever(interactor.getPlansWithOwnership()).thenReturn(
            Single.just(mapOf(Pair(standardSkuDetails, true), Pair(premiumSkuDetails, false)))
        )

        whenever(standardSkuDetails.price).thenReturn(STANDARD_PRICE)
        whenever(standardSkuDetails.sku).thenReturn(InAppPurchase.StandardSubscriptionPlan.sku)
        whenever(premiumSkuDetails.price).thenReturn(PREMIUM_PRICE)
        whenever(premiumSkuDetails.sku).thenReturn(InAppPurchase.PremiumSubscriptionPlan.sku)


        presenter = SubscriptionsPresenter(view, interactor, identityManager)
    }

    @Test
    fun presentPlansNoOwnedTest() {
        whenever(interactor.getPlansWithOwnership()).thenReturn(
            Single.just(mapOf(Pair(standardSkuDetails, false), Pair(premiumSkuDetails, false)))
        )

        presenter.subscribe()

        verify(interactor).getPlansWithOwnership()

        verify(view).presentStandardPlan(STANDARD_PRICE)
        verify(view).presentPremiumPlan(PREMIUM_PRICE)
    }

    @Test
    fun presentPlansOneOwnedTest() {

        presenter.subscribe()

        verify(interactor).getPlansWithOwnership()

        verify(view).presentStandardPlan(null)
        verify(view).presentPremiumPlan(PREMIUM_PRICE)
        verify(view).presentCancelInfo(true)
    }

    @Test
    fun presentNoPlansTest() {
        whenever(interactor.getPlansWithOwnership()).thenReturn(Single.just(mapOf()))

        presenter.subscribe()

        verify(interactor).getPlansWithOwnership()

        verify(view, never()).presentStandardPlan(STANDARD_PRICE)
        verify(view, never()).presentPremiumPlan(PREMIUM_PRICE)
        verify(view).presentCancelInfo(false)
    }

    @Test
    fun subscriptionListenerTest() {
        presenter.subscribe()

        verify(interactor).addSubscriptionListener(presenter)

        presenter.unsubscribe()

        verify(interactor).removeSubscriptionListener(presenter)
    }

    @Test
    fun cancelSubscriptionInfoTest() {
        whenever(view.cancelSubscriptionInfoClicks).thenReturn(Observable.just(Unit))

        presenter.subscribe()

        verify(view).redirectToPlayStoreSubscriptions()
    }

    @Test
    fun buySubscriptionWithoutLogin() {
        whenever(view.premiumSubscriptionClicks).thenReturn(Observable.just(Unit))

        presenter.subscribe()

        verify(view).navigateToLogin()
        verify(interactor, never()).purchasePremiumPlan()
    }

    @Test
    fun buySubscription() {
        whenever(view.premiumSubscriptionClicks).thenReturn(Observable.just(Unit))
        whenever(identityManager.isLoggedIn).thenReturn(true)

        presenter.subscribe()

        verify(interactor).purchasePremiumPlan()
        verify(interactor, never()).purchaseStandardPlan()
    }
}
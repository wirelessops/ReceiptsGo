package co.smartreceipts.android.purchases.subscriptions

import co.smartreceipts.android.apis.WebServiceManager
import co.smartreceipts.android.identity.IdentityManager
import co.smartreceipts.android.purchases.apis.subscriptions.SubscriptionsApiResponse
import co.smartreceipts.android.purchases.apis.subscriptions.SubscriptionsApiService
import co.smartreceipts.android.purchases.model.InAppPurchase
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class RemoteSubscriptionManagerTest {

    private lateinit var remoteSubscriptionManager: RemoteSubscriptionManager

    @Mock
    private lateinit var purchaseWallet: PurchaseWallet

    @Mock
    private lateinit var webServiceManager: WebServiceManager

    @Mock
    private lateinit var identityManager: IdentityManager

    @Mock
    private lateinit var subscriptionApiResponseValidator: SubscriptionApiResponseValidator

    @Mock
    private lateinit var subscriptionsApiService: SubscriptionsApiService

    @Mock
    private lateinit var subscriptionsApiResponse: SubscriptionsApiResponse

    private val subscriptionSet: Set<RemoteSubscription> = setOf(RemoteSubscription(5, InAppPurchase.SmartReceiptsPlus, Date()))

    private val signInStream = PublishSubject.create<Boolean>()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(webServiceManager.getService(SubscriptionsApiService::class.java)).thenReturn(subscriptionsApiService)
        whenever(subscriptionsApiService.getSubscriptions()).thenReturn(Observable.just(subscriptionsApiResponse))
        whenever(identityManager.isLoggedInStream).thenReturn(signInStream)
        whenever(subscriptionApiResponseValidator.getActiveSubscriptions(subscriptionsApiResponse)).thenReturn(subscriptionSet)
        remoteSubscriptionManager = RemoteSubscriptionManager(
            purchaseWallet, webServiceManager, identityManager, subscriptionApiResponseValidator, Schedulers.trampoline()
        )
    }

    @Test
    fun initializeWhenSignedInAndPlusIsNotOwned() {
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(false, true)

        val testSubscriber = remoteSubscriptionManager.getNewRemoteSubscriptions().test()

        signInStream.onNext(true)

        testSubscriber.assertValue(subscriptionSet)
            .assertNoErrors()
        verify(purchaseWallet).updateRemotePurchases(subscriptionSet)
    }

    @Test
    fun initializeWhenSignedInAndPlusIsOwned() {
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(true)

        val testSubscriber = remoteSubscriptionManager.getNewRemoteSubscriptions().test()

        signInStream.onNext(true)

        testSubscriber.assertValue(Collections.emptySet())
            .assertNoErrors()
        verify(purchaseWallet).updateRemotePurchases(subscriptionSet)
    }

    @Test
    fun initializeWhenNotSignedIn() {
        val testSubscriber = remoteSubscriptionManager.getNewRemoteSubscriptions().test()
        signInStream.onNext(false)
        testSubscriber.assertNoValues()
        testSubscriber.assertNoErrors()
        verifyZeroInteractions(purchaseWallet)
    }

    @Test
    fun initializeWithError() {
        whenever(subscriptionsApiService.getSubscriptions()).thenReturn(Observable.error(Exception("Test")))
        val testSubscriber = remoteSubscriptionManager.getNewRemoteSubscriptions().test()
        signInStream.onNext(true)
        testSubscriber.assertValue(Collections.emptySet())
        testSubscriber.assertNoErrors()
        verifyZeroInteractions(purchaseWallet)
    }
}
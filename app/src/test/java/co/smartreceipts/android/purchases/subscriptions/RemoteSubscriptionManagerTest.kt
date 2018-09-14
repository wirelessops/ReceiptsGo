package co.smartreceipts.android.purchases.subscriptions

import co.smartreceipts.android.apis.hosts.ServiceManager
import co.smartreceipts.android.purchases.apis.subscriptions.SubscriptionsApiResponse
import co.smartreceipts.android.purchases.apis.subscriptions.SubscriptionsApiService
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RemoteSubscriptionManagerTest {

    private lateinit var remoteSubscriptionManager: RemoteSubscriptionManager

    @Mock
    private lateinit var purchaseWallet: PurchaseWallet

    @Mock
    private lateinit var serviceManager: ServiceManager

    @Mock
    private lateinit var subscriptionApiResponseValidator: SubscriptionApiResponseValidator

    @Mock
    private lateinit var subscriptionsApiService: SubscriptionsApiService

    @Mock
    private lateinit var subscriptionsApiResponse: SubscriptionsApiResponse

    @Mock
    private lateinit var subscriptionSet: Set<RemoteSubscription>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(serviceManager.getService(SubscriptionsApiService::class.java)).thenReturn(subscriptionsApiService)
        whenever(subscriptionsApiService.getSubscriptions()).thenReturn(Observable.just(subscriptionsApiResponse))
        whenever(subscriptionApiResponseValidator.getActiveSubscriptions(subscriptionsApiResponse)).thenReturn(subscriptionSet)
        remoteSubscriptionManager = RemoteSubscriptionManager(purchaseWallet, serviceManager, subscriptionApiResponseValidator)
    }

    @Test
    fun initialize() {
        remoteSubscriptionManager.initialize()
        verify(purchaseWallet).updateRemotePurchases(subscriptionSet)
    }
}
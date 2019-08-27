package co.smartreceipts.android.identity.widget.account

import co.smartreceipts.android.identity.IdentityManager
import co.smartreceipts.android.identity.apis.organizations.Organization
import co.smartreceipts.android.identity.apis.organizations.OrganizationModel
import co.smartreceipts.android.identity.apis.organizations.OrganizationUser
import co.smartreceipts.android.identity.organization.OrganizationManager
import co.smartreceipts.android.identity.store.EmailAddress
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker
import co.smartreceipts.android.purchases.model.InAppPurchase
import co.smartreceipts.android.purchases.subscriptions.RemoteSubscription
import co.smartreceipts.android.purchases.subscriptions.RemoteSubscriptionManager
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*
import kotlin.NoSuchElementException

@RunWith(RobolectricTestRunner::class)
class AccountInteractorTest {

    // Class under test
    private lateinit var interactor: AccountInteractor

    private val identityManager = mock<IdentityManager>()
    private val organizationManager = mock<OrganizationManager>()
    private val remoteSubscriptionManager = mock<RemoteSubscriptionManager>()
    private val ocrPurchaseTracker = mock<OcrPurchaseTracker>()
    private val organization = mock<Organization>()


    @Before
    fun setUp() {
        interactor = AccountInteractor(
            identityManager, organizationManager, ocrPurchaseTracker, remoteSubscriptionManager,
            Schedulers.trampoline(), Schedulers.trampoline()
        )
    }

    @Test
    fun getEmailTest() {
        val emailAddress = EmailAddress("sample@gmail.com")
        whenever(identityManager.email).thenReturn(emailAddress)

        assertEquals(emailAddress, interactor.getEmail())
    }

    @Test
    fun getOrganizationSuccessTest() {
        whenever(organizationManager.getOrganizations()).thenReturn(Maybe.just(listOf(organization)))
        whenever(organizationManager.checkOrganizationSettingsMatch(organization)).thenReturn(Single.just(false))

        val expectedOrganizationModel = OrganizationModel(organization, OrganizationUser.UserRole.ADMIN, false)

        val testObserver = interactor.getOrganizations().test()
        testObserver.awaitTerminalEvent()
        testObserver.assertValueCount(1)
            .assertResult(listOf(expectedOrganizationModel))
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun getOrganizationEmptyTest() {
        whenever(organizationManager.getOrganizations()).thenReturn(Maybe.empty())

        val testObserver = interactor.getOrganizations().test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertResult(Collections.emptyList())
    }

    @Test
    fun getOrganizationErrorTest() {
        whenever(organizationManager.getOrganizations()).thenReturn(Maybe.error(Exception()))

        val testObserver = interactor.getOrganizations().test()
        testObserver.awaitTerminalEvent()
        testObserver.assertNotComplete()
            .assertNoValues()
            .assertError(Exception::class.java)

    }

    @Test
    fun applyOrganizationSettingsSuccessTest() {
        whenever(organizationManager.applyOrganizationSettings(organization)).thenReturn(Completable.complete())

        val testObserver = interactor.applyOrganizationSettings(organization).test()
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
            .assertNoErrors()
    }

    @Test
    fun applyOrganizationSettingsErrorTest() {
        whenever(organizationManager.applyOrganizationSettings(organization)).thenReturn(Completable.error(Exception()))

        val testObserver = interactor.applyOrganizationSettings(organization).test()
        testObserver.awaitTerminalEvent()
        testObserver.assertNotComplete()
            .assertError(Exception::class.java)
    }

    @Test
    fun getOcrRemainingScansStreamTest() {
        whenever(ocrPurchaseTracker.remainingScansStream).thenReturn(Observable.just(5))

        val testObserver = interactor.getOcrRemainingScansStream().test()
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
            .assertNoErrors()
            .assertResult(5)
    }

    @Test
    fun getSubscriptionsEmptyTest() {
        whenever(remoteSubscriptionManager.getNewRemoteSubscriptions()).thenReturn(Observable.just(Collections.emptySet()))

        val testObserver = interactor.getSubscriptionsStream().test()
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
            .assertNoErrors()
            .assertNoValues()
    }

    @Test
    fun getSubscriptionsTest() {

        val list = listOf(
            RemoteSubscription(5, InAppPurchase.SmartReceiptsPlus, Date()),
            RemoteSubscription(6, InAppPurchase.OcrScans10, Date())
        )

        whenever(remoteSubscriptionManager.getNewRemoteSubscriptions()).thenReturn(Observable.just(list.toSet()))

        val testObserver = interactor.getSubscriptionsStream().test()
        testObserver.awaitTerminalEvent()

        testObserver.assertComplete()
            .assertNoErrors()
            .assertResult(list)
    }

    @Test
    fun updateOrganizationSettingsTest() {
        whenever(organizationManager.updateOrganizationSettings(organization)).thenReturn(Completable.complete())

        val testObserver = interactor.uploadOrganizationSettings(organization).test()
        testObserver.awaitTerminalEvent()

        testObserver.assertComplete()
    }

    @Test
    fun updateOrganizationSettingsErrorTest() {
        whenever(organizationManager.updateOrganizationSettings(organization)).thenReturn(Completable.error(NoSuchElementException()))

        val testObserver = interactor.uploadOrganizationSettings(organization).test()
        testObserver.awaitTerminalEvent()

        testObserver.assertNotComplete()
            .assertError(NoSuchElementException::class.java)
    }
}
package co.smartreceipts.android.identity.widget.account

import co.smartreceipts.android.identity.IdentityManager
import co.smartreceipts.android.identity.OrganizationManager
import co.smartreceipts.android.identity.apis.organizations.Organization
import co.smartreceipts.android.identity.apis.organizations.OrganizationUser
import co.smartreceipts.android.identity.store.EmailAddress
import co.smartreceipts.android.widget.model.UiIndicator
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AccountInteractorTest {

    // Class under test
    private lateinit var interactor: AccountInteractor

    private val identityManager = mock<IdentityManager>()
    private val organizationManager = mock<OrganizationManager>()
    private val organization = mock<Organization>()


    @Before
    fun setUp() {
        interactor = AccountInteractor(identityManager, organizationManager, Schedulers.trampoline(), Schedulers.trampoline())
    }

    @Test
    fun getEmailTest() {
        val emailAddress = EmailAddress("sample@gmail.com")
        whenever(identityManager.email).thenReturn(emailAddress)

        assertEquals(emailAddress, interactor.getEmail())
    }

    @Test
    fun getOrganizationSuccessTest() {
        whenever(organizationManager.getPrimaryOrganization()).thenReturn(Maybe.just(organization))
        whenever(organizationManager.checkOrganizationSettingsMatch(organization)).thenReturn(Single.just(false))

        val expectedOrganizationModel = AccountInteractor.OrganizationModel(organization, OrganizationUser.UserRole.USER, false)

        val testObserver = interactor.getOrganization().test()
        testObserver.awaitTerminalEvent()
        testObserver.assertValueCount(2)
            .assertValueAt(0, UiIndicator.loading())
            .assertValueAt(1, UiIndicator.success(expectedOrganizationModel))
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun getOrganizationEmptyTest() {
        whenever(organizationManager.getPrimaryOrganization()).thenReturn(Maybe.empty())

        val testObserver = interactor.getOrganization().test()
        testObserver.awaitTerminalEvent()
        testObserver.assertValueCount(2)
            .assertValues(UiIndicator.loading(), UiIndicator.idle())
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun getOrganizationErrorTest() {
        whenever(organizationManager.getPrimaryOrganization()).thenReturn(Maybe.error(Exception()))

        val testObserver = interactor.getOrganization().test()
        testObserver.awaitTerminalEvent()
        testObserver.assertValueCount(2)
            .assertValues(UiIndicator.loading(), UiIndicator.error())
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun applyOrganizationSettingsSuccessTest() {
        whenever(organizationManager.applyOrganizationSettings(organization)).thenReturn(Completable.complete())

        val testObserver = interactor.applyOrganizationSettings(organization).test()
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
            .assertNoErrors()
            .assertValues(UiIndicator.success())
    }

    @Test
    fun applyOrganizationSettingsErrorTest() {
        whenever(organizationManager.applyOrganizationSettings(organization)).thenReturn(Completable.error(Exception()))

        val testObserver = interactor.applyOrganizationSettings(organization).test()
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
            .assertNoErrors()
            .assertValues(UiIndicator.error())
    }


}
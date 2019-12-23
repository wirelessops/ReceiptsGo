package co.smartreceipts.android.identity.widget.account

import co.smartreceipts.android.identity.apis.organizations.Organization
import co.smartreceipts.android.identity.apis.organizations.OrganizationModel
import co.smartreceipts.core.identity.store.EmailAddress
import co.smartreceipts.android.purchases.model.InAppPurchase
import co.smartreceipts.android.purchases.subscriptions.RemoteSubscription
import co.smartreceipts.android.widget.model.UiIndicator
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class AccountPresenterTest {

    companion object {
        private const val EMAIL = "sample@sample.com"
    }

    // Class under test
    private lateinit var presenter: AccountPresenter

    private val view = mock<AccountView>()
    private val interactor = mock<AccountInteractor>()

    private val organizationModel = mock<OrganizationModel>()
    private val organization = mock<Organization>()

    @Before
    fun setUp() {
        whenever(view.applySettingsClicks).thenReturn(Observable.never())
        whenever(view.logoutButtonClicks).thenReturn(Observable.never())
        whenever(view.uploadSettingsClicks).thenReturn(Observable.never())

        whenever(interactor.getEmail()).thenReturn(EmailAddress(EMAIL))
        whenever(interactor.getOrganizations()).thenReturn(Single.just(Collections.emptyList()))
        whenever(interactor.getOcrRemainingScansStream()).thenReturn(Observable.just(5))
        whenever(interactor.getSubscriptionsStream()).thenReturn(Observable.empty())

        whenever(organizationModel.organization).thenReturn(organization)


        presenter = AccountPresenter(view, interactor)
    }

    @Test
    fun presentEmailTest() {
        presenter.subscribe()

        verify(view).presentEmail(eq(EmailAddress(EMAIL)))
    }

    @Test
    fun presentNoOrganization() {
        presenter.subscribe()

        verify(view, times(2)).presentOrganizations(any())
        verify(view).presentOrganizations(UiIndicator.loading())
        verify(view).presentOrganizations(UiIndicator.idle())

        verify(interactor, times(1)).getOrganizations()
    }

    @Test
    fun presentOrganizations() {
        val organizationModel = mock<OrganizationModel>()
        whenever(interactor.getOrganizations()).thenReturn(Single.just(listOf(organizationModel)))
        whenever(organizationModel.organization).thenReturn(mock<Organization>())

        presenter.subscribe()

        verify(view, times(2)).presentOrganizations(any())
        verify(view).presentOrganizations(UiIndicator.loading())
        verify(view).presentOrganizations(UiIndicator.success(listOf(organizationModel)))

        verify(interactor, times(1)).getOrganizations()
    }

    @Test
    fun presentOcrScansTest() {
        presenter.subscribe()

        verify(view).presentOcrScans(5)
    }

    @Test
    fun presentSubscriptions() {

        val subscriptions = listOf(RemoteSubscription(45, InAppPurchase.SmartReceiptsPlus, Date()))
        whenever(interactor.getSubscriptionsStream()).thenReturn(Observable.just(subscriptions))

        presenter.subscribe()

        verify(view).presentSubscriptions(subscriptions)
    }

    @Test
    fun applySettingsClickTest() {
        whenever(view.applySettingsClicks).thenReturn(Observable.just(organizationModel))

        presenter.subscribe()

        verify(interactor).applyOrganizationSettings(organizationModel.organization)
    }

    @Test
    fun uploadSettingsClickTest() {
        whenever(view.uploadSettingsClicks).thenReturn(Observable.just(organizationModel))

        presenter.subscribe()

        verify(interactor).uploadOrganizationSettings(organizationModel.organization)
    }
}
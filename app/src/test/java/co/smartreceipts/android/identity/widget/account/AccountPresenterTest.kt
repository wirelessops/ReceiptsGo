package co.smartreceipts.android.identity.widget.account

import co.smartreceipts.android.identity.store.EmailAddress
import co.smartreceipts.android.widget.model.UiIndicator
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AccountPresenterTest {

    companion object {
        private const val EMAIL = "sample@sample.com"
    }

    // Class under test
    private lateinit var presenter: AccountPresenter

    private val view = mock<AccountView>()
    private val interactor = mock<AccountInteractor>()

    @Before
    fun setUp() {
        whenever(view.applySettingsClicks).thenReturn(Observable.never())
        whenever(view.logoutButtonClicks).thenReturn(Observable.never())

        whenever(interactor.getEmail()).thenReturn(EmailAddress(EMAIL))
        whenever(interactor.getOrganization()).thenReturn(Observable.just(UiIndicator.loading(), UiIndicator.idle()))

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

        verify(view, times(2)).presentOrganization(any())
        verify(view).presentOrganization(UiIndicator.loading())
        verify(view).presentOrganization(UiIndicator.loading())
        verify(view).presentOrganization(UiIndicator.idle())

        verify(interactor, times(1)).getOrganization()
    }

    @Test
    fun presentOrganization() {
        val organizationModel = mock<AccountInteractor.OrganizationModel>()
        whenever(interactor.getOrganization()).thenReturn(Observable.just(UiIndicator.loading(), UiIndicator.success(organizationModel)))

        presenter.subscribe()

        verify(view, times(2)).presentOrganization(any())
        verify(view).presentOrganization(UiIndicator.loading())
        verify(view).presentOrganization(UiIndicator.success(organizationModel))

        verify(interactor, times(1)).getOrganization()
    }
}
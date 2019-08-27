package co.smartreceipts.android.receipts.editor.paymentmethods

import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PaymentMethodsPresenterTest  {

    @Mock
    private lateinit var view: PaymentMethodsView

    @Mock
    private lateinit var userPreferenceManager: UserPreferenceManager

    @Mock
    private lateinit var togglePaymentMethodFieldVisibilityConsumer: Consumer<Boolean>

    private val userPreferenceChangeStream = PublishSubject.create<UserPreference<*>>()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        Mockito.doReturn(togglePaymentMethodFieldVisibilityConsumer).whenever(view).togglePaymentMethodFieldVisibility()
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.UsePaymentMethods)).thenReturn(Single.just(true))
        whenever(userPreferenceManager.userPreferenceChangeStream).thenReturn(userPreferenceChangeStream)
    }

    @Test
    fun subscribeWithPaymentMethodsEnabled() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.UsePaymentMethods)).thenReturn(Single.just(true))
        val presenter = PaymentMethodsPresenter(view, userPreferenceManager, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        Mockito.verify(togglePaymentMethodFieldVisibilityConsumer).accept(true)
    }

    @Test
    fun subscribeWithPaymentMethodsDisabled() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.UsePaymentMethods)).thenReturn(Single.just(false))
        val presenter = PaymentMethodsPresenter(view, userPreferenceManager, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        Mockito.verify(togglePaymentMethodFieldVisibilityConsumer).accept(false)
    }

    @Test
    fun subscribeAndChangePaymentMethodsFromEnabledToDisabled() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.UsePaymentMethods)).thenReturn(Single.just(true), Single.just(false))
        val presenter = PaymentMethodsPresenter(view, userPreferenceManager, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        Mockito.verify(togglePaymentMethodFieldVisibilityConsumer).accept(true)
        userPreferenceChangeStream.onNext(UserPreference.Receipts.UsePaymentMethods)
        Mockito.verify(togglePaymentMethodFieldVisibilityConsumer).accept(false)
    }

    @Test
    fun subscribeAndChangeOtherPreferencesDoesNothing() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.UsePaymentMethods)).thenReturn(Single.just(true))
        val presenter = PaymentMethodsPresenter(view, userPreferenceManager, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        Mockito.verify(togglePaymentMethodFieldVisibilityConsumer).accept(true)
        userPreferenceChangeStream.onNext(UserPreference.General.DateSeparator)
        verifyNoMoreInteractions(togglePaymentMethodFieldVisibilityConsumer)
    }

}
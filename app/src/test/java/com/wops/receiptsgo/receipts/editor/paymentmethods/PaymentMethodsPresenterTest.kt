package com.wops.receiptsgo.receipts.editor.paymentmethods

import com.wops.receiptsgo.model.PaymentMethod
import com.wops.receiptsgo.persistence.database.controllers.impl.PaymentMethodsTableController
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.nhaarman.mockitokotlin2.mock
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

    private val paymentMethodsTableController = mock<PaymentMethodsTableController>()

    private val userPreferenceChangeStream = PublishSubject.create<UserPreference<*>>()

    private val paymentMethod1: PaymentMethod = mock()
    private val paymentMethod2: PaymentMethod = mock()
    private val paymentMethod3: PaymentMethod = PaymentMethod.NONE
    private val mockList = mutableListOf(paymentMethod1, paymentMethod2)
    private val mockListWithCompanion = mutableListOf(paymentMethod1, paymentMethod2, paymentMethod3)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        Mockito.doReturn(togglePaymentMethodFieldVisibilityConsumer).whenever(view).togglePaymentMethodFieldVisibility()
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.UsePaymentMethods)).thenReturn(Single.just(true))
        whenever(userPreferenceManager.userPreferenceChangeStream).thenReturn(userPreferenceChangeStream)
        whenever(paymentMethodsTableController.get()).thenReturn(Single.just(mockList))
    }

    @Test
    fun subscribeWithPaymentMethodsEnabled() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.UsePaymentMethods)).thenReturn(Single.just(true))
        val presenter = PaymentMethodsPresenter(view, userPreferenceManager, Schedulers.trampoline(), Schedulers.trampoline(), paymentMethodsTableController)
        presenter.subscribe()
        Mockito.verify(togglePaymentMethodFieldVisibilityConsumer).accept(true)
        Mockito.verify(view).displayPaymentMethods(mockListWithCompanion)
    }

    @Test
    fun subscribeWithPaymentMethodsDisabled() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.UsePaymentMethods)).thenReturn(Single.just(false))
        val presenter = PaymentMethodsPresenter(view, userPreferenceManager, Schedulers.trampoline(), Schedulers.trampoline(), paymentMethodsTableController)
        presenter.subscribe()
        Mockito.verify(togglePaymentMethodFieldVisibilityConsumer).accept(false)
    }

    @Test
    fun subscribeAndChangePaymentMethodsFromEnabledToDisabled() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.UsePaymentMethods)).thenReturn(Single.just(true), Single.just(false))
        val presenter = PaymentMethodsPresenter(view, userPreferenceManager, Schedulers.trampoline(), Schedulers.trampoline(), paymentMethodsTableController)
        presenter.subscribe()
        Mockito.verify(togglePaymentMethodFieldVisibilityConsumer).accept(true)
        userPreferenceChangeStream.onNext(UserPreference.Receipts.UsePaymentMethods)
        Mockito.verify(togglePaymentMethodFieldVisibilityConsumer).accept(false)
    }

    @Test
    fun subscribeAndChangeOtherPreferencesDoesNothing() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.UsePaymentMethods)).thenReturn(Single.just(true))
        val presenter = PaymentMethodsPresenter(view, userPreferenceManager, Schedulers.trampoline(), Schedulers.trampoline(), paymentMethodsTableController)
        presenter.subscribe()
        Mockito.verify(togglePaymentMethodFieldVisibilityConsumer).accept(true)
        userPreferenceChangeStream.onNext(UserPreference.General.DateSeparator)
        verifyNoMoreInteractions(togglePaymentMethodFieldVisibilityConsumer)
    }

}
package co.smartreceipts.android.receipts.editor.pricing

import android.os.Bundle

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions

@RunWith(RobolectricTestRunner::class)
class ReceiptPricingPresenterTest {

    @Mock
    private lateinit var view: ReceiptPricingView

    @Mock
    private lateinit var userPreferenceManager: UserPreferenceManager

    @Mock
    private lateinit var receipt: Receipt

    @Mock
    private lateinit var price: Price

    @Mock
    private lateinit var tax: Price

    @Mock
    private lateinit var displayReceiptPriceConsumer: Consumer<Price>

    @Mock
    private lateinit var displayReceiptTaxConsumer: Consumer<Price>

    @Mock
    private lateinit var toggleReceiptTaxFieldVisibilityConsumer: Consumer<Boolean>

    private val userPreferenceChangeStream = PublishSubject.create<UserPreference<*>>()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        doReturn(displayReceiptPriceConsumer).whenever(view).displayReceiptPrice()
        doReturn(displayReceiptTaxConsumer).whenever(view).displayReceiptTax()
        doReturn(toggleReceiptTaxFieldVisibilityConsumer).whenever(view).toggleReceiptTaxFieldVisibility()
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(true))
        whenever(userPreferenceManager.userPreferenceChangeStream).thenReturn(userPreferenceChangeStream)
        whenever(receipt.price).thenReturn(price)
        whenever(receipt.tax).thenReturn(tax)
    }

    @Test
    fun subscribeWithTaxesEnabled() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(true))
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, receipt, null, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verify(toggleReceiptTaxFieldVisibilityConsumer).accept(true)
    }

    @Test
    fun subscribeWithTaxesDisabled() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(false))
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, receipt, null, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verify(toggleReceiptTaxFieldVisibilityConsumer).accept(false)
    }

    @Test
    fun subscribeAndChangeTaxesFromEnabledToDisabled() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(true), Single.just(false))
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, receipt, null, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verify(toggleReceiptTaxFieldVisibilityConsumer).accept(true)
        userPreferenceChangeStream.onNext(UserPreference.Receipts.IncludeTaxField)
        verify(toggleReceiptTaxFieldVisibilityConsumer).accept(false)
    }

    @Test
    fun subscribeAndChangeOtherPreferencesDoesNothing() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(true))
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, receipt, null, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verify(toggleReceiptTaxFieldVisibilityConsumer).accept(true)
        userPreferenceChangeStream.onNext(UserPreference.General.DateSeparator)
        verifyNoMoreInteractions(toggleReceiptTaxFieldVisibilityConsumer)
    }

    @Test
    fun subscribeWithNullReceipt() {
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, null, null, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verifyZeroInteractions(displayReceiptPriceConsumer)
        verifyZeroInteractions(displayReceiptTaxConsumer)
    }

    @Test
    fun subscribeWithReceiptAndNullState() {
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, receipt, null, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verify<Consumer<Price>>(displayReceiptPriceConsumer).accept(price)
        verify<Consumer<Price>>(displayReceiptTaxConsumer).accept(tax)
    }

    @Test
    fun subscribeWithReceiptAndNonNullState() {
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, receipt, Bundle(), Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verifyZeroInteractions(displayReceiptPriceConsumer)
        verifyZeroInteractions(displayReceiptTaxConsumer)
    }

}
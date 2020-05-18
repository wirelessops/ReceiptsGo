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
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
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
    private lateinit var tax2: Price

    @Mock
    private lateinit var displayReceiptPriceConsumer: Consumer<Price>

    @Mock
    private lateinit var displayReceiptTaxConsumer: Consumer<Price>

    @Mock
    private lateinit var displayReceiptTax2Consumer: Consumer<Price>

    @Mock
    private lateinit var toggleReceiptTaxFieldVisibilityConsumer: Consumer<Boolean>

    @Mock
    private lateinit var toggleReceiptTax2FieldVisibilityConsumer: Consumer<Boolean>

    private val userPreferenceChangeStream = PublishSubject.create<UserPreference<*>>()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        doReturn(displayReceiptPriceConsumer).whenever(view).displayReceiptPrice()
        doReturn(displayReceiptTaxConsumer).whenever(view).displayReceiptTax()
        doReturn(displayReceiptTax2Consumer).whenever(view).displayReceiptTax2()
        doReturn(toggleReceiptTaxFieldVisibilityConsumer).whenever(view).toggleReceiptTaxFieldVisibility()
        doReturn(toggleReceiptTax2FieldVisibilityConsumer).whenever(view).toggleReceiptTax2FieldVisibility()
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(true))
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTax2Field)).thenReturn(Single.just(false))
        whenever(userPreferenceManager.userPreferenceChangeStream).thenReturn(userPreferenceChangeStream)
        whenever(receipt.price).thenReturn(price)
        whenever(receipt.tax).thenReturn(tax)
        whenever(receipt.tax2).thenReturn(tax2)
    }

    @Test
    fun subscribeWithTwoTaxesEnabled() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(true))
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTax2Field)).thenReturn(Single.just(true))
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, receipt, null, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verify(toggleReceiptTaxFieldVisibilityConsumer).accept(true)
        verify(toggleReceiptTax2FieldVisibilityConsumer).accept(true)
    }

    @Test
    fun subscribeWithOneTaxEnabled() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(true))
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTax2Field)).thenReturn(Single.just(false))
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, receipt, null, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verify(toggleReceiptTaxFieldVisibilityConsumer).accept(true)
        verify(toggleReceiptTax2FieldVisibilityConsumer).accept(false)
    }

    @Test
    fun subscribeWithTaxesDisabled() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(false))
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTax2Field)).thenReturn(Single.just(false))
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, receipt, null, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verify(toggleReceiptTaxFieldVisibilityConsumer).accept(false)
        verify(toggleReceiptTax2FieldVisibilityConsumer).accept(false)
    }

    @Test
    fun subscribeAndChangeTaxFromEnabledToDisabled() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(true), Single.just(false))
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, receipt, null, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verify(toggleReceiptTaxFieldVisibilityConsumer).accept(true)
        userPreferenceChangeStream.onNext(UserPreference.Receipts.IncludeTaxField)
        verify(toggleReceiptTaxFieldVisibilityConsumer).accept(false)
    }

    @Test
    fun subscribeAndChangeTax2FromEnabledToDisabled() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTax2Field)).thenReturn(Single.just(true), Single.just(false))
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, receipt, null, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verify(toggleReceiptTax2FieldVisibilityConsumer).accept(true)
        userPreferenceChangeStream.onNext(UserPreference.Receipts.IncludeTax2Field)
        verify(toggleReceiptTax2FieldVisibilityConsumer).accept(false)
    }

    @Test
    fun subscribeAndChangeOtherPreferencesDoesNothing() {
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(true))
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTax2Field)).thenReturn(Single.just(true))
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, receipt, null, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verify(toggleReceiptTaxFieldVisibilityConsumer).accept(true)
        verify(toggleReceiptTax2FieldVisibilityConsumer).accept(true)
        userPreferenceChangeStream.onNext(UserPreference.General.DateSeparator)
        verifyNoMoreInteractions(toggleReceiptTaxFieldVisibilityConsumer)
        verifyNoMoreInteractions(toggleReceiptTax2FieldVisibilityConsumer)
    }

    @Test
    fun subscribeWithNullReceipt() {
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, null, null, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verifyZeroInteractions(displayReceiptPriceConsumer)
        verifyZeroInteractions(displayReceiptTaxConsumer)
        verifyZeroInteractions(displayReceiptTax2Consumer)
    }

    @Test
    fun subscribeWithReceiptAndNullState() {
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, receipt, null, Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verify(displayReceiptPriceConsumer).accept(price)
        verify(displayReceiptTaxConsumer).accept(tax)
        verify(displayReceiptTax2Consumer).accept(tax2)
    }

    @Test
    fun subscribeWithReceiptAndNonNullState() {
        val presenter = ReceiptPricingPresenter(view, userPreferenceManager, receipt, Bundle(), Schedulers.trampoline(), Schedulers.trampoline())
        presenter.subscribe()
        verifyZeroInteractions(displayReceiptPriceConsumer)
        verifyZeroInteractions(displayReceiptTaxConsumer)
        verifyZeroInteractions(displayReceiptTax2Consumer)
    }

}
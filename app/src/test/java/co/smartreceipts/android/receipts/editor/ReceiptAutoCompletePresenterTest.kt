package co.smartreceipts.android.receipts.editor

import co.smartreceipts.android.DefaultObjects
import co.smartreceipts.android.autocomplete.AutoCompleteResult
import co.smartreceipts.android.autocomplete.receipt.ReceiptAutoCompleteField
import co.smartreceipts.android.model.AutoCompleteUpdateEvent
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController
import co.smartreceipts.android.purchases.PurchaseManager
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import co.smartreceipts.android.settings.UserPreferenceManager
import com.hadisatrio.optional.Optional
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReceiptAutoCompletePresenterTest {

    // Class under test
    private lateinit var presenter: ReceiptCreateEditFragmentPresenter

    private lateinit var receipt:Receipt
    private lateinit var autoCompleteUpdateEventName:AutoCompleteUpdateEvent<Receipt>
    private lateinit var autoCompleteResult: AutoCompleteResult<Receipt>

    private val userPreferenceManager = mock<UserPreferenceManager>()
    private val receiptTableController = mock<ReceiptTableController>()
    private val purchaseManager = mock<PurchaseManager>()
    private val purchaseWallet = mock<PurchaseWallet>()

    private val view = mock<ReceiptCreateEditFragment>()
    private val newReceipt = mock<Receipt>()

    @Before
    fun setUp() {
        receipt = ReceiptBuilderFactory().setTrip(DefaultObjects.newDefaultTrip()).build()
        autoCompleteResult = AutoCompleteResult(receipt.name, receipt)
        autoCompleteUpdateEventName = AutoCompleteUpdateEvent(autoCompleteResult, ReceiptAutoCompleteField.Name, 0)

        presenter = ReceiptCreateEditFragmentPresenter(view, userPreferenceManager, purchaseManager, purchaseWallet, receiptTableController)

        whenever(view.hideAutoCompleteVisibilityClick).thenReturn(Observable.never())
        whenever(view.unHideAutoCompleteVisibilityClick).thenReturn(Observable.never())
    }

    @Test
    fun hideAutoCompleteValueSucceeds() {
        whenever(view.hideAutoCompleteVisibilityClick).thenReturn(Observable.just(autoCompleteUpdateEventName))
        whenever(presenter.updateReceipt(receipt, ReceiptBuilderFactory(receipt)
                .setNameHiddenFromAutoComplete(true)
                .build())).thenReturn(Observable.just(Optional.of(newReceipt)))

        presenter.subscribe()

        verify(view).removeValueFromAutoComplete(autoCompleteUpdateEventName.position)
    }

    @Test
    fun unHideAutoCompleteValueSucceeds() {
        whenever(view.unHideAutoCompleteVisibilityClick).thenReturn(Observable.just(autoCompleteUpdateEventName))
        whenever(presenter.updateReceipt(receipt, ReceiptBuilderFactory(receipt)
                .setNameHiddenFromAutoComplete(false)
                .build())).thenReturn(Observable.just(Optional.of(newReceipt)))

        presenter.subscribe()

        verify(view).sendAutoCompleteUnHideEvent(autoCompleteUpdateEventName.position)
    }

    @Test
    fun unHideAutoCompleteValueErrors() {
        whenever(view.unHideAutoCompleteVisibilityClick).thenReturn(Observable.just(autoCompleteUpdateEventName))
        whenever(presenter.updateReceipt(receipt, ReceiptBuilderFactory(receipt)
                .setNameHiddenFromAutoComplete(false)
                .build())).thenReturn(Observable.just(Optional.absent()))

        presenter.subscribe()

        verify(view).displayAutoCompleteError()
    }
}
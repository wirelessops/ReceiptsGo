package co.smartreceipts.android.receipts.editor.toolbar

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import co.smartreceipts.android.R
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.persistence.DatabaseHelper
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

/**
 *
 */
@RunWith(RobolectricTestRunner::class)
class ReceiptsEditorToolbarInteractorTest {

    companion object {
        private const val DATABASE_ID = 5
        private const val RECEIPT_ID = 2
    }

    private lateinit var interactor: ReceiptsEditorToolbarInteractor

    @Mock
    private lateinit var databaseHelper: DatabaseHelper

    @Mock
    private lateinit var preferenceManager: UserPreferenceManager

    @Mock
    private lateinit var receipt: Receipt

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(databaseHelper.nextReceiptAutoIncrementIdHelper).thenReturn(Single.just(DATABASE_ID))
        whenever(receipt.id).thenReturn(RECEIPT_ID)
        interactor = ReceiptsEditorToolbarInteractor(ApplicationProvider.getApplicationContext(), databaseHelper, preferenceManager, Schedulers.trampoline())
    }

    @Test
    fun getTitleForNewReceiptWithoutId() {
        whenever(preferenceManager.getSingle(UserPreference.Receipts.ShowReceiptID)).thenReturn(Single.just(false))
        interactor.getReceiptTitle(null)
                .test()
                .assertValue(ApplicationProvider.getApplicationContext<Context>().getString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW))
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun getTitleForNewReceiptWithId() {
        whenever(preferenceManager.getSingle(UserPreference.Receipts.ShowReceiptID)).thenReturn(Single.just(true))
        interactor.getReceiptTitle(null)
                .test()
                .assertValue(ApplicationProvider.getApplicationContext<Context>().getString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW_ID, DATABASE_ID))
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun getTitleForExistingReceiptWithoutId() {
        whenever(preferenceManager.getSingle(UserPreference.Receipts.ShowReceiptID)).thenReturn(Single.just(false))
        interactor.getReceiptTitle(receipt)
                .test()
                .assertValue(ApplicationProvider.getApplicationContext<Context>().getString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT))
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun getTitleForExistingReceiptWithId() {
        whenever(preferenceManager.getSingle(UserPreference.Receipts.ShowReceiptID)).thenReturn(Single.just(true))
        interactor.getReceiptTitle(receipt)
                .test()
                .assertValue(ApplicationProvider.getApplicationContext<Context>().getString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT_ID, RECEIPT_ID))
                .assertComplete()
                .assertNoErrors()
    }
}
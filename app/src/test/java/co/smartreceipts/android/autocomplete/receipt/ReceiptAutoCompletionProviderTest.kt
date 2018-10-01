package co.smartreceipts.android.autocomplete.receipt

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReceiptAutoCompletionProviderTest {

    private lateinit var provider: ReceiptAutoCompletionProvider

    @Mock
    private lateinit var tableController: ReceiptTableController

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        provider = ReceiptAutoCompletionProvider(tableController)
    }

    @Test
    fun getAutoCompletionType() {
        assertEquals(Receipt::class.java, provider.autoCompletionType)
    }

    @Test
    fun getTableController() {
        assertEquals(tableController, provider.tableController)
    }

    @Test
    fun getSupportedAutoCompleteFields() {
        assertEquals(listOf(ReceiptAutoCompleteField.Name, ReceiptAutoCompleteField.Comment), provider.supportedAutoCompleteFields)
    }
}
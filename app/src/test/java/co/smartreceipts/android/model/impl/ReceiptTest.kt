package co.smartreceipts.android.model.impl

import co.smartreceipts.android.DefaultObjects
import co.smartreceipts.android.date.DisplayableDate
import co.smartreceipts.android.model.*
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory
import co.smartreceipts.android.persistence.DatabaseHelper
import co.smartreceipts.core.sync.model.SyncState
import co.smartreceipts.android.utils.TestLocaleToggler
import co.smartreceipts.android.utils.testParcel
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.sql.Date
import java.util.*

@RunWith(RobolectricTestRunner::class)
class ReceiptTest {

    companion object {

        private const val ID = 5
        private val REC_UUID = UUID.randomUUID()
        private const val NAME = "Name"
        private val DATE = Date(1409703721000L)
        private val TIMEZONE = TimeZone.getDefault()
        private val DISPLAYABLE_DATE = DisplayableDate(DATE, TIMEZONE)
        private const val COMMENT = "Comment"
        private const val REIMBURSABLE = true
        private const val INDEX = 3
        private const val FULL_PAGE = true
        private const val IS_SELECTED = true
        private const val EXTRA1 = "extra1"
        private const val EXTRA2 = "extra2"
        private const val EXTRA3 = "extra3"
        private const val CUSTOM_ORDER: Long = 2
        private const val NAME_HIDDEN_AUTO_COMPLETE = false
        private const val COMMENT_HIDDEN_AUTO_COMPLETE = false
    }

    // Class under test
    private lateinit var receipt: Receipt

    private lateinit var trip: Trip
    private lateinit var file: File
    private lateinit var paymentMethod: PaymentMethod
    private lateinit var category: Category
    private lateinit var price: Price
    private lateinit var tax: Price
    private lateinit var syncState: SyncState

    @Before
    @Throws(Exception::class)
    fun setUp() {
        TestLocaleToggler.setDefaultLocale(Locale.US)
        trip = DefaultObjects.newDefaultTrip()
        file = File(File("").absolutePath)
        paymentMethod = DefaultObjects.newDefaultPaymentMethod()
        category = DefaultObjects.newDefaultCategory()
        price = DefaultObjects.newDefaultPrice()
        tax = DefaultObjects.newDefaultTax()
        syncState = DefaultObjects.newDefaultSyncState()
        receipt = Receipt(
            ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
            FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
        )
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        TestLocaleToggler.resetDefaultLocale()
    }

    @Test
    fun getId() {
        assertEquals(ID, receipt.id)
    }

    @Test
    fun getUuid() {
        assertEquals(REC_UUID, receipt.uuid)
    }

    @Test
    fun getTrip() {
        assertEquals(trip, receipt.trip)
    }

    @Test
    fun getPaymentMethod() {
        assertEquals(paymentMethod, receipt.paymentMethod)
    }

    @Test
    fun getName() {
        assertEquals(NAME, receipt.name)
    }

    @Test
    fun getFile() {
        assertEquals(file, receipt.file)
    }

    @Test
    fun hasPDF() {
        val pdfFile = File("file.pdf")
        val pdfFileWithUpperCaseExtension = File("file.PDF")
        val pdfReceipt = Receipt(
                ID, REC_UUID, INDEX, trip, pdfFile, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
        )
        val pdfReceiptWithUpperCaseExtension = Receipt(
                ID, REC_UUID, INDEX, trip, pdfFileWithUpperCaseExtension, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
        )
        assertFalse(receipt.hasPDF())
        assertTrue(pdfReceipt.hasPDF())
        assertTrue(pdfReceiptWithUpperCaseExtension.hasPDF())
    }

    @Test
    fun hasImage() {
        val pngFile = File("file.png")
        val jpgFile = File("file.jpg")
        val jpegFile = File("file.jpeg")
        val pngFileWithUpperCaseExtension = File("file.PNG")
        val jpgFileWithUpperCaseExtension = File("file.JPG")
        val jpegFileWithUpperCaseExtension = File("file.JPEG")

        val pngReceipt = Receipt(
                ID, REC_UUID, INDEX, trip, pngFile, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
        )
        val jpgReceipt = Receipt(
                ID, REC_UUID, INDEX, trip, jpgFile, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
        )
        val jpegReceipt = Receipt(
                ID, REC_UUID, INDEX, trip, jpegFile, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
        )
        val pngReceiptWithUpperCaseExtension = Receipt(
                ID, REC_UUID, INDEX, trip, pngFileWithUpperCaseExtension, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
        )
        val jpgReceiptWithUpperCaseExtension = Receipt(
                ID, REC_UUID, INDEX, trip, jpgFileWithUpperCaseExtension, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
        )
        val jpegReceiptWithUpperCaseExtension = Receipt(
                ID, REC_UUID, INDEX, trip, jpegFileWithUpperCaseExtension, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
        )
        assertFalse(receipt.hasImage())
        assertTrue(pngReceipt.hasImage())
        assertTrue(jpgReceipt.hasImage())
        assertTrue(jpegReceipt.hasImage())
        assertTrue(pngReceiptWithUpperCaseExtension.hasImage())
        assertTrue(jpgReceiptWithUpperCaseExtension.hasImage())
        assertTrue(jpegReceiptWithUpperCaseExtension.hasImage())
    }

    @Test
    fun getCategory() {
        assertEquals(category, receipt.category)
    }

    @Test
    fun getComment() {
        assertEquals(COMMENT, receipt.comment)
    }

    @Test
    fun getPrice() {
        assertEquals(price, receipt.price)
    }

    @Test
    fun getTax() {
        assertEquals(tax, receipt.tax)
    }

    @Test
    fun getDate() {
        assertEquals(DATE, receipt.date)
    }

    @Test
    fun getTimeZone() {
        assertEquals(TIMEZONE, receipt.timeZone)
    }

    @Test
    fun isReimbursable() {
        assertEquals(REIMBURSABLE, receipt.isReimbursable)
    }

    @Test
    fun isFullPage() {
        assertEquals(FULL_PAGE, receipt.isFullPage)
    }

    @Test
    fun isSelected() {
        assertEquals(IS_SELECTED, receipt.isSelected)
    }

    @Test
    fun getIndex() {
        assertEquals(INDEX, receipt.index)
    }

    @Test
    fun getExtraEditText1() {
        val nullExtra1Receipt = Receipt(
            ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE,
            IS_SELECTED, null, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
        )
        val noDataExtra1Receipt = ReceiptBuilderFactory(nullExtra1Receipt).setExtraEditText1(DatabaseHelper.NO_DATA).build()

        assertTrue(receipt.hasExtraEditText1())
        assertEquals(EXTRA1, receipt.extraEditText1)
        assertFalse(nullExtra1Receipt.hasExtraEditText1())
        assertNull(nullExtra1Receipt.extraEditText1)
        assertFalse(noDataExtra1Receipt.hasExtraEditText1())
        assertNull(noDataExtra1Receipt.extraEditText1)
    }

    @Test
    fun getExtraEditText2() {
        val nullExtra2Receipt = Receipt(
            ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE,
            IS_SELECTED, EXTRA1, null, EXTRA3, syncState, CUSTOM_ORDER
        )
        val noDataExtra2Receipt = ReceiptBuilderFactory(nullExtra2Receipt).setExtraEditText2(DatabaseHelper.NO_DATA).build()

        assertTrue(receipt.hasExtraEditText2())
        assertEquals(EXTRA2, receipt.extraEditText2)
        assertFalse(nullExtra2Receipt.hasExtraEditText2())
        assertNull(nullExtra2Receipt.extraEditText2)
        assertFalse(noDataExtra2Receipt.hasExtraEditText2())
        assertNull(noDataExtra2Receipt.extraEditText2)
    }

    @Test
    fun getExtraEditText3() {
        val nullExtra3Receipt = Receipt(
            ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE,
            IS_SELECTED, EXTRA1, EXTRA2, null, syncState, CUSTOM_ORDER
        )
        val noDataExtra3Receipt = ReceiptBuilderFactory(nullExtra3Receipt).setExtraEditText3(DatabaseHelper.NO_DATA).build()

        assertTrue(receipt.hasExtraEditText3())
        assertEquals(EXTRA3, receipt.extraEditText3)
        assertFalse(nullExtra3Receipt.hasExtraEditText3())
        assertNull(nullExtra3Receipt.extraEditText3)
        assertFalse(noDataExtra3Receipt.hasExtraEditText3())
        assertNull(noDataExtra3Receipt.extraEditText3)
    }

    @Test
    fun getSyncState() {
        assertEquals(syncState, receipt.syncState)
    }

    @Test
    fun compareTo() {
        assertTrue(
            receipt.compareTo(
                Receipt(
                    ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                    FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                )
            ) == 0
        )
        assertTrue(
                receipt > Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DisplayableDate(Date(DATE.time * 2), TIMEZONE),
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                )
        )
        assertTrue(
                receipt < Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DisplayableDate(Date(0), TIMEZONE),
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                )
        )
    }

    @Test
    fun testHashCode() {
        assertEquals(receipt.hashCode().toLong(), receipt.hashCode().toLong())
        assertEquals(
            receipt.hashCode().toLong(),
            Receipt(
                ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
            ).hashCode().toLong()
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        -1, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE,
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX + 1, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, 
                            DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, mock(Trip::class.java), file, paymentMethod, NAME, category, COMMENT, price, tax, 
                            DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, mock(File::class.java), paymentMethod, NAME, category, COMMENT, price, tax,
                            DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, mock(PaymentMethod::class.java), NAME, category, COMMENT, price, tax,
                            DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, "bad", category, COMMENT, price, tax, DISPLAYABLE_DATE,
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, mock(Category::class.java), COMMENT, price, tax,
                            DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, "bad", price, tax, DISPLAYABLE_DATE,
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, mock(Price::class.java), tax,
                            DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, mock(Price::class.java),
                            DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax,
                        DisplayableDate(Date(System.currentTimeMillis()), TIMEZONE), REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2,
                        EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, !REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                        !FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, "bad", EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, EXTRA1, "bad", EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, "bad", syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )

        // Special equals cases (we don't check on the sources don't count, and selected doesn't count):
        assertEquals(
            receipt.hashCode().toLong(),
            Receipt(
                ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
            ).hashCode().toLong()
        )
        assertEquals(
            receipt.hashCode().toLong(),
            Receipt(
                ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                FULL_PAGE, !IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
            ).hashCode().toLong()
        )
    }

    @Test
    fun testEquals() {
        assertEquals(receipt, receipt)
        assertEquals(
            receipt,
            Receipt(
                ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
            )
        )
        assertThat(receipt, not(equalTo(Any())))
        assertThat(receipt, not(equalTo(mock(Receipt::class.java))))
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        -1, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE,
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX + 1, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE,
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, mock(Trip::class.java), file, paymentMethod, NAME, category, COMMENT, price, tax,
                            DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, mock(File::class.java), paymentMethod, NAME, category, COMMENT, price, tax,
                            DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, mock(PaymentMethod::class.java), NAME, category, COMMENT, price, tax,
                            DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, "bad", category, COMMENT, price, tax, DISPLAYABLE_DATE,
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, mock(Category::class.java), COMMENT, price, tax,
                            DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, "bad", price, tax, DISPLAYABLE_DATE,
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, mock(Price::class.java), tax,
                            DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, mock(Price::class.java),
                        DISPLAYABLE_DATE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax,
                        DisplayableDate(Date(System.currentTimeMillis()), TIMEZONE), REIMBURSABLE, FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2,
                        EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, !REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                        !FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, "bad", EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, EXTRA1, "bad", EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, "bad", syncState, CUSTOM_ORDER
                    )
                )
            )
        )

        // Special equals cases (we don't check on the sources don't count, and selected doesn't count):
        assertEquals(
            receipt,
            Receipt(
                ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
            )
        )
        assertEquals(
            receipt,
            Receipt(
                ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DISPLAYABLE_DATE, REIMBURSABLE,
                FULL_PAGE, !IS_SELECTED, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
            )
        )
    }

    @Test
    fun parcelEquality() {

        val receiptFromParcel = receipt.testParcel()

        assertNotSame(receipt, receiptFromParcel)
        assertEquals(receipt, receiptFromParcel)
    }

}
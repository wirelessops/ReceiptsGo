package co.smartreceipts.android.model.impl

import co.smartreceipts.android.DefaultObjects
import co.smartreceipts.android.model.*
import co.smartreceipts.android.persistence.DatabaseHelper
import co.smartreceipts.android.sync.model.SyncState
import co.smartreceipts.android.utils.TestLocaleToggler
import co.smartreceipts.android.utils.testParcel
import junit.framework.Assert.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertThat
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

        private val ID = 5
        private val REC_UUID = UUID.randomUUID()
        private val NAME = "Name"
        private val DATE = Date(1409703721000L)
        private val TIMEZONE = TimeZone.getDefault()
        private val COMMENT = "Comment"
        private val REIMBURSABLE = true
        private val INDEX = 3
        private val FULL_PAGE = true
        private val IS_SELECTED = true
        private val EXTRA1 = "extra1"
        private val EXTRA2 = "extra2"
        private val EXTRA3 = "extra3"
        private val CUSTOM_ORDER: Long = 2
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
            ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
            FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
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
            ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE,
            IS_SELECTED, Source.Undefined, null, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
        )
        val noDataExtra1Receipt = Receipt(
            ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE,
            IS_SELECTED, Source.Undefined, DatabaseHelper.NO_DATA, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
        )

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
            ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE,
            IS_SELECTED, Source.Undefined, EXTRA1, null, EXTRA3, syncState, CUSTOM_ORDER
        )
        val noDataExtra2Receipt = Receipt(
            ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE,
            IS_SELECTED, Source.Undefined, EXTRA1, DatabaseHelper.NO_DATA, EXTRA3, syncState, CUSTOM_ORDER
        )

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
            ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE,
            IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, null, syncState, CUSTOM_ORDER
        )
        val noDataExtra3Receipt = Receipt(
            ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE,
            IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, DatabaseHelper.NO_DATA, syncState, CUSTOM_ORDER
        )

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
                    ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                    FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                )
            ) == 0
        )
        assertTrue(
            receipt.compareTo(
                Receipt(
                    ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, Date(DATE.time * 2), TIMEZONE,
                    REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                )
            ) > 0
        )
        assertTrue(
            receipt.compareTo(
                Receipt(
                    ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, Date(0), TIMEZONE,
                    REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                )
            ) < 0
        )
    }

    @Test
    fun testHashCode() {
        Assert.assertEquals(receipt.hashCode().toLong(), receipt.hashCode().toLong())
        Assert.assertEquals(
            receipt.hashCode().toLong(),
            Receipt(
                ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
            ).hashCode().toLong()
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        -1, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE,
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX + 1, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE,
                        TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, mock(Trip::class.java), file, paymentMethod, NAME, category, COMMENT, price, tax, DATE,
                        TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, mock(File::class.java), paymentMethod, NAME, category, COMMENT, price, tax, DATE,
                        TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, mock(PaymentMethod::class.java), NAME, category, COMMENT, price, tax, DATE,
                        TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, "bad", category, COMMENT, price, tax, DATE, TIMEZONE,
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, mock(Category::class.java), COMMENT, price, tax, DATE,
                        TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, "bad", price, tax, DATE, TIMEZONE,
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, mock(Price::class.java), tax, DATE,
                        TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, mock(Price::class.java), DATE,
                        TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
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
                        Date(System.currentTimeMillis()), TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2,
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
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, !REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                        !FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, Source.Undefined, "bad", EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, "bad", EXTRA3, syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )
        assertThat(
            receipt.hashCode(),
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, "bad", syncState, CUSTOM_ORDER
                    ).hashCode()
                )
            )
        )

        // Special equals cases (we don't check on the sources don't count, and selected doesn't count):
        Assert.assertEquals(
            receipt.hashCode().toLong(),
            Receipt(
                ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, Source.Parcel, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
            ).hashCode().toLong()
        )
        Assert.assertEquals(
            receipt.hashCode().toLong(),
            Receipt(
                ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                FULL_PAGE, !IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
            ).hashCode().toLong()
        )
    }

    @Test
    fun testEquals() {
        Assert.assertEquals(receipt, receipt)
        Assert.assertEquals(
            receipt,
            Receipt(
                ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
            )
        )
        assertThat(receipt, not(equalTo(Any())))
        assertThat(receipt, not(equalTo(mock(Receipt::class.java))))
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        -1, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE,
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX + 1, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE,
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, mock(Trip::class.java), file, paymentMethod, NAME, category, COMMENT, price, tax, DATE,
                        TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, mock(File::class.java), paymentMethod, NAME, category, COMMENT, price, tax, DATE,
                        TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, mock(PaymentMethod::class.java), NAME, category, COMMENT, price, tax, DATE,
                        TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, "bad", category, COMMENT, price, tax, DATE, TIMEZONE,
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, mock(Category::class.java), COMMENT, price, tax, DATE,
                        TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, "bad", price, tax, DATE, TIMEZONE,
                        REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, mock(Price::class.java), tax, DATE,
                        TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, mock(Price::class.java), DATE,
                        TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
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
                        Date(System.currentTimeMillis()), TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2,
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
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, !REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                        !FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, Source.Undefined, "bad", EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, "bad", EXTRA3, syncState, CUSTOM_ORDER
                    )
                )
            )
        )
        assertThat(
            receipt,
            not(
                equalTo(
                    Receipt(
                        ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                        FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, "bad", syncState, CUSTOM_ORDER
                    )
                )
            )
        )

        // Special equals cases (we don't check on the sources don't count, and selected doesn't count):
        Assert.assertEquals(
            receipt,
            Receipt(
                ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                FULL_PAGE, IS_SELECTED, Source.Parcel, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
            )
        )
        Assert.assertEquals(
            receipt,
            Receipt(
                ID, REC_UUID, INDEX, trip, file, paymentMethod, NAME, category, COMMENT, price, tax, DATE, TIMEZONE, REIMBURSABLE,
                FULL_PAGE, !IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, syncState, CUSTOM_ORDER
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
package co.smartreceipts.android.model.impl

import android.os.Parcel
import co.smartreceipts.android.DefaultObjects
import co.smartreceipts.android.model.*
import co.smartreceipts.android.persistence.DatabaseHelper
import co.smartreceipts.android.sync.model.SyncState
import co.smartreceipts.android.utils.TestLocaleToggler
import junit.framework.Assert.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertNotNull
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
class DefaultReceiptImplTest {

    // Class under test
    private lateinit var mReceipt: DefaultReceiptImpl

    private lateinit var mTrip: Trip
    private lateinit var mFile: File
    private lateinit var mPaymentMethod: PaymentMethod
    private lateinit var mCategory: Category
    private lateinit var mPrice: Price
    private lateinit var mTax: Price
    private lateinit var mSyncState: SyncState

    @Before
    @Throws(Exception::class)
    fun setUp() {
        TestLocaleToggler.setDefaultLocale(Locale.US)
        mTrip = DefaultObjects.newDefaultTrip()
        mFile = File(File("").absolutePath)
        mPaymentMethod = DefaultObjects.newDefaultPaymentMethod()
        mCategory = DefaultObjects.newDefaultCategory()
        mPrice = DefaultObjects.newDefaultPrice()
        mTax = DefaultObjects.newDefaultTax()
        mSyncState = DefaultObjects.newDefaultSyncState()
        mReceipt = DefaultReceiptImpl(
            ID,
            INDEX,
            mTrip,
            mFile,
            mPaymentMethod,
            NAME,
            mCategory,
            COMMENT,
            mPrice,
            mTax,
            DATE,
            TIMEZONE,
            REIMBURSABLE,
            FULL_PAGE,
            IS_SELECTED,
            Source.Undefined,
            EXTRA1,
            EXTRA2,
            EXTRA3,
            mSyncState,
            CUSTOM_ORDER.toLong()
        )
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        TestLocaleToggler.resetDefaultLocale()
    }

    @Test
    fun getId() {
        assertEquals(ID, mReceipt.id)
    }

    @Test
    fun getTrip() {
        assertEquals(mTrip, mReceipt.trip)
    }

    @Test
    fun getPaymentMethod() {
        assertEquals(mPaymentMethod, mReceipt.paymentMethod)
    }

    @Test
    fun getName() {
        assertEquals(NAME, mReceipt.name)
    }

    @Test
    fun getFile() {
        assertEquals(mFile, mReceipt.file)
    }

    @Test
    fun getCategory() {
        assertEquals(mCategory, mReceipt.category)
    }

    @Test
    fun getComment() {
        assertEquals(COMMENT, mReceipt.comment)
    }

    @Test
    fun getPrice() {
        assertEquals(mPrice, mReceipt.price)
    }

    @Test
    fun getTax() {
        assertEquals(mTax, mReceipt.tax)
    }

    @Test
    fun getDate() {
        assertEquals(DATE, mReceipt.date)
    }

    @Test
    fun getTimeZone() {
        assertEquals(TIMEZONE, mReceipt.timeZone)
    }

    @Test
    fun isReimbursable() {
        assertEquals(REIMBURSABLE, mReceipt.isReimbursable)
    }

    @Test
    fun isFullPage() {
        assertEquals(FULL_PAGE, mReceipt.isFullPage)
    }

    @Test
    fun isSelected() {
        assertEquals(IS_SELECTED, mReceipt.isSelected)
    }

    @Test
    fun getIndex() {
        assertEquals(INDEX, mReceipt.index)
    }

    @Test
    fun getExtraEditText1() {
        val nullExtra1Receipt = DefaultReceiptImpl(
            ID,
            INDEX,
            mTrip,
            mFile,
            mPaymentMethod,
            NAME,
            mCategory,
            COMMENT,
            mPrice,
            mTax,
            DATE,
            TIMEZONE,
            REIMBURSABLE,
            FULL_PAGE,
            IS_SELECTED,
            Source.Undefined,
            null,
            EXTRA2,
            EXTRA3,
            mSyncState,
            CUSTOM_ORDER.toLong()
        )
        val noDataExtra1Receipt = DefaultReceiptImpl(
            ID,
            INDEX,
            mTrip,
            mFile,
            mPaymentMethod,
            NAME,
            mCategory,
            COMMENT,
            mPrice,
            mTax,
            DATE,
            TIMEZONE,
            REIMBURSABLE,
            FULL_PAGE,
            IS_SELECTED,
            Source.Undefined,
            DatabaseHelper.NO_DATA,
            EXTRA2,
            EXTRA3,
            mSyncState,
            CUSTOM_ORDER.toLong()
        )

        assertTrue(mReceipt.hasExtraEditText1())
        assertEquals(EXTRA1, mReceipt.extraEditText1)
        assertFalse(nullExtra1Receipt.hasExtraEditText1())
        assertNull(nullExtra1Receipt.extraEditText1)
        assertFalse(noDataExtra1Receipt.hasExtraEditText1())
        assertNull(noDataExtra1Receipt.extraEditText1)
    }

    @Test
    fun getExtraEditText2() {
        val nullExtra2Receipt = DefaultReceiptImpl(
            ID,
            INDEX,
            mTrip,
            mFile,
            mPaymentMethod,
            NAME,
            mCategory,
            COMMENT,
            mPrice,
            mTax,
            DATE,
            TIMEZONE,
            REIMBURSABLE,
            FULL_PAGE,
            IS_SELECTED,
            Source.Undefined,
            EXTRA1,
            null,
            EXTRA3,
            mSyncState,
            CUSTOM_ORDER.toLong()
        )
        val noDataExtra2Receipt = DefaultReceiptImpl(
            ID,
            INDEX,
            mTrip,
            mFile,
            mPaymentMethod,
            NAME,
            mCategory,
            COMMENT,
            mPrice,
            mTax,
            DATE,
            TIMEZONE,
            REIMBURSABLE,
            FULL_PAGE,
            IS_SELECTED,
            Source.Undefined,
            EXTRA1,
            DatabaseHelper.NO_DATA,
            EXTRA3,
            mSyncState,
            CUSTOM_ORDER.toLong()
        )

        assertTrue(mReceipt.hasExtraEditText2())
        assertEquals(EXTRA2, mReceipt.extraEditText2)
        assertFalse(nullExtra2Receipt.hasExtraEditText2())
        assertNull(nullExtra2Receipt.extraEditText2)
        assertFalse(noDataExtra2Receipt.hasExtraEditText2())
        assertNull(noDataExtra2Receipt.extraEditText2)
    }

    @Test
    fun getExtraEditText3() {
        val nullExtra3Receipt = DefaultReceiptImpl(
            ID,
            INDEX,
            mTrip,
            mFile,
            mPaymentMethod,
            NAME,
            mCategory,
            COMMENT,
            mPrice,
            mTax,
            DATE,
            TIMEZONE,
            REIMBURSABLE,
            FULL_PAGE,
            IS_SELECTED,
            Source.Undefined,
            EXTRA1,
            EXTRA2,
            null,
            mSyncState,
            CUSTOM_ORDER.toLong()
        )
        val noDataExtra3Receipt = DefaultReceiptImpl(
            ID,
            INDEX,
            mTrip,
            mFile,
            mPaymentMethod,
            NAME,
            mCategory,
            COMMENT,
            mPrice,
            mTax,
            DATE,
            TIMEZONE,
            REIMBURSABLE,
            FULL_PAGE,
            IS_SELECTED,
            Source.Undefined,
            EXTRA1,
            EXTRA2,
            DatabaseHelper.NO_DATA,
            mSyncState,
            CUSTOM_ORDER.toLong()
        )

        assertTrue(mReceipt.hasExtraEditText3())
        assertEquals(EXTRA3, mReceipt.extraEditText3)
        assertFalse(nullExtra3Receipt.hasExtraEditText3())
        assertNull(nullExtra3Receipt.extraEditText3)
        assertFalse(noDataExtra3Receipt.hasExtraEditText3())
        assertNull(noDataExtra3Receipt.extraEditText3)
    }

    @Test
    fun getSyncState() {
        assertEquals(mSyncState, mReceipt.syncState)
    }

    @Test
    fun compareTo() {
        assertTrue(
            mReceipt.compareTo(
                DefaultReceiptImpl(
                    ID,
                    INDEX,
                    mTrip,
                    mFile,
                    mPaymentMethod,
                    NAME,
                    mCategory,
                    COMMENT,
                    mPrice,
                    mTax,
                    DATE,
                    TIMEZONE,
                    REIMBURSABLE,
                    FULL_PAGE,
                    IS_SELECTED,
                    Source.Undefined,
                    EXTRA1,
                    EXTRA2,
                    EXTRA3,
                    mSyncState,
                    CUSTOM_ORDER.toLong()
                )
            ) == 0
        )
        assertTrue(
            mReceipt.compareTo(
                DefaultReceiptImpl(
                    ID,
                    INDEX,
                    mTrip,
                    mFile,
                    mPaymentMethod,
                    NAME,
                    mCategory,
                    COMMENT,
                    mPrice,
                    mTax,
                    Date(DATE.time * 2),
                    TIMEZONE,
                    REIMBURSABLE,
                    FULL_PAGE,
                    IS_SELECTED,
                    Source.Undefined,
                    EXTRA1,
                    EXTRA2,
                    EXTRA3,
                    mSyncState,
                    CUSTOM_ORDER.toLong()
                )
            ) > 0
        )
        assertTrue(
            mReceipt.compareTo(
                DefaultReceiptImpl(
                    ID,
                    INDEX,
                    mTrip,
                    mFile,
                    mPaymentMethod,
                    NAME,
                    mCategory,
                    COMMENT,
                    mPrice,
                    mTax,
                    Date(0),
                    TIMEZONE,
                    REIMBURSABLE,
                    FULL_PAGE,
                    IS_SELECTED,
                    Source.Undefined,
                    EXTRA1,
                    EXTRA2,
                    EXTRA3,
                    mSyncState,
                    CUSTOM_ORDER.toLong()
                )
            ) < 0
        )
    }

    @Test
    fun testHashCode() {
        Assert.assertEquals(mReceipt.hashCode().toLong(), mReceipt.hashCode().toLong())
        Assert.assertEquals(
            mReceipt.hashCode().toLong(),
            DefaultReceiptImpl(
                ID,
                INDEX,
                mTrip,
                mFile,
                mPaymentMethod,
                NAME,
                mCategory,
                COMMENT,
                mPrice,
                mTax,
                DATE,
                TIMEZONE,
                REIMBURSABLE,
                FULL_PAGE,
                IS_SELECTED,
                Source.Undefined,
                EXTRA1,
                EXTRA2,
                EXTRA3,
                mSyncState,
                CUSTOM_ORDER.toLong()
            ).hashCode().toLong()
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        -1,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX + 1,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mock(Trip::class.java),
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mock(File::class.java),
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mock(PaymentMethod::class.java),
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        "bad",
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mock(Category::class.java),
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        "bad",
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mock(Price::class.java),
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mock(Price::class.java),
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        Date(System.currentTimeMillis()),
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        !REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        !FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        "bad",
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        "bad",
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )
        assertThat(
            mReceipt.hashCode(),
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        "bad",
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    ).hashCode()
                )
            )
        )

        // Special equals cases (we don't check on the sources don't count, and selected doesn't count):
        Assert.assertEquals(
            mReceipt.hashCode().toLong(),
            DefaultReceiptImpl(
                ID,
                INDEX,
                mTrip,
                mFile,
                mPaymentMethod,
                NAME,
                mCategory,
                COMMENT,
                mPrice,
                mTax,
                DATE,
                TIMEZONE,
                REIMBURSABLE,
                FULL_PAGE,
                IS_SELECTED,
                Source.Parcel,
                EXTRA1,
                EXTRA2,
                EXTRA3,
                mSyncState,
                CUSTOM_ORDER.toLong()
            ).hashCode().toLong()
        )
        Assert.assertEquals(
            mReceipt.hashCode().toLong(),
            DefaultReceiptImpl(
                ID,
                INDEX,
                mTrip,
                mFile,
                mPaymentMethod,
                NAME,
                mCategory,
                COMMENT,
                mPrice,
                mTax,
                DATE,
                TIMEZONE,
                REIMBURSABLE,
                FULL_PAGE,
                !IS_SELECTED,
                Source.Undefined,
                EXTRA1,
                EXTRA2,
                EXTRA3,
                mSyncState,
                CUSTOM_ORDER.toLong()
            ).hashCode().toLong()
        )
    }

    @Test
    fun testEquals() {
        Assert.assertEquals(mReceipt, mReceipt)
        Assert.assertEquals(
            mReceipt,
            DefaultReceiptImpl(
                ID,
                INDEX,
                mTrip,
                mFile,
                mPaymentMethod,
                NAME,
                mCategory,
                COMMENT,
                mPrice,
                mTax,
                DATE,
                TIMEZONE,
                REIMBURSABLE,
                FULL_PAGE,
                IS_SELECTED,
                Source.Undefined,
                EXTRA1,
                EXTRA2,
                EXTRA3,
                mSyncState,
                CUSTOM_ORDER.toLong()
            )
        )
        assertThat(mReceipt, not(equalTo(Any())))
        assertThat(mReceipt, not(equalTo(mock(Receipt::class.java))))
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        -1,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX + 1,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mock(Trip::class.java),
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mock(File::class.java),
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mock(PaymentMethod::class.java),
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        "bad",
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mock(Category::class.java),
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        "bad",
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mock(Price::class.java),
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mock(Price::class.java),
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        Date(System.currentTimeMillis()),
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        !REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        !FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        "bad",
                        EXTRA2,
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        "bad",
                        EXTRA3,
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )
        assertThat(
            mReceipt,
            not(
                equalTo(
                    DefaultReceiptImpl(
                        ID,
                        INDEX,
                        mTrip,
                        mFile,
                        mPaymentMethod,
                        NAME,
                        mCategory,
                        COMMENT,
                        mPrice,
                        mTax,
                        DATE,
                        TIMEZONE,
                        REIMBURSABLE,
                        FULL_PAGE,
                        IS_SELECTED,
                        Source.Undefined,
                        EXTRA1,
                        EXTRA2,
                        "bad",
                        mSyncState,
                        CUSTOM_ORDER.toLong()
                    )
                )
            )
        )

        // Special equals cases (we don't check on the sources don't count, and selected doesn't count):
        Assert.assertEquals(
            mReceipt,
            DefaultReceiptImpl(
                ID,
                INDEX,
                mTrip,
                mFile,
                mPaymentMethod,
                NAME,
                mCategory,
                COMMENT,
                mPrice,
                mTax,
                DATE,
                TIMEZONE,
                REIMBURSABLE,
                FULL_PAGE,
                IS_SELECTED,
                Source.Parcel,
                EXTRA1,
                EXTRA2,
                EXTRA3,
                mSyncState,
                CUSTOM_ORDER.toLong()
            )
        )
        Assert.assertEquals(
            mReceipt,
            DefaultReceiptImpl(
                ID,
                INDEX,
                mTrip,
                mFile,
                mPaymentMethod,
                NAME,
                mCategory,
                COMMENT,
                mPrice,
                mTax,
                DATE,
                TIMEZONE,
                REIMBURSABLE,
                FULL_PAGE,
                !IS_SELECTED,
                Source.Undefined,
                EXTRA1,
                EXTRA2,
                EXTRA3,
                mSyncState,
                CUSTOM_ORDER.toLong()
            )
        )
    }

    @Test
    fun parcelEquality() {
        val parcel = Parcel.obtain()
        mReceipt.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val receipt = DefaultReceiptImpl.CREATOR.createFromParcel(parcel)
        assertNotNull(receipt)
        assertEquals(receipt, mReceipt)
    }

    companion object {

        private val ID = 5
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
        private val CUSTOM_ORDER = 2
    }

}
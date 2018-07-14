package co.smartreceipts.android.model.comparators

import co.smartreceipts.android.model.Receipt
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.sql.Date

@RunWith(RobolectricTestRunner::class)
class ReceiptDateComparatorTest {

    private lateinit var ascendingComparator: ReceiptDateComparator
    private lateinit var descendingComparator: ReceiptDateComparator
    private lateinit var defaultComparator: ReceiptDateComparator

    @Mock
    private var first: Receipt? = null
    @Mock
    private var second: Receipt? = null

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        // DESC places the 'most recent' date at the front of the list
        ascendingComparator = ReceiptDateComparator(true)
        descendingComparator = ReceiptDateComparator(false)
        defaultComparator = ReceiptDateComparator() // same as ascendingComparator

        val now = System.currentTimeMillis()
        whenever(first!!.date).thenReturn(Date(now))
        whenever(second!!.date).thenReturn(Date(now + 10000L))
    }

    @Test
    fun compareNullFirstToNullSecond() {
        assertTrue(ascendingComparator.compare(null, null) == 0)
        assertTrue(descendingComparator.compare(null, null) == 0)
        assertTrue(defaultComparator.compare(null, null) == 0)
    }

    @Test
    fun compareFirstToNullSecond() {
        assertTrue(ascendingComparator.compare(first, null) > 0)
        assertTrue(descendingComparator.compare(first, null) > 0)
        assertTrue(defaultComparator.compare(first, null) > 0)
    }

    @Test
    fun compareNullFirstToSecond() {
        assertTrue(ascendingComparator.compare(null, second) < 0)
        assertTrue(descendingComparator.compare(null, second) < 0)
        assertTrue(defaultComparator.compare(null, second) < 0)
    }

    @Test
    fun compareFirstToFirst() {
        assertTrue(ascendingComparator.compare(first, first) == 0)
        assertTrue(descendingComparator.compare(first, first) == 0)
        assertTrue(defaultComparator.compare(first, first) == 0)
    }

    @Test
    fun compareFirstToSecond() {
        assertTrue(ascendingComparator.compare(first, second) < 0)
        assertTrue(descendingComparator.compare(first, second) > 0)
        assertTrue(defaultComparator.compare(first, second) < 0)
    }

    @Test
    fun compareSecondToFirst() {
        assertTrue(ascendingComparator.compare(second, first) > 0)
        assertTrue(descendingComparator.compare(second, first) < 0)
        assertTrue(defaultComparator.compare(second, first) > 0)
    }

}

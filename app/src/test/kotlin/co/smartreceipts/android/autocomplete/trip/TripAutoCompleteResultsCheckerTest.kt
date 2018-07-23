package co.smartreceipts.android.autocomplete.trip

import co.smartreceipts.android.autocomplete.AutoCompleteField
import co.smartreceipts.android.model.Trip
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TripAutoCompleteResultsCheckerTest {

    companion object {
        private const val NAME = "name"
        private const val COMMENT = "comment"
        private const val COST = "cost"
    }

    private val resultsChecker: TripAutoCompleteResultsChecker = TripAutoCompleteResultsChecker()

    @Mock
    private lateinit var trip: Trip

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(trip.name).thenReturn(NAME)
        whenever(trip.comment).thenReturn(COMMENT)
        whenever(trip.costCenter).thenReturn(COST)
    }

    @Test
    fun matchesInput() {
        assertTrue(resultsChecker.matchesInput("n", TripAutoCompleteField.Name, trip))
        assertTrue(resultsChecker.matchesInput("na", TripAutoCompleteField.Name, trip))
        assertTrue(resultsChecker.matchesInput("nam", TripAutoCompleteField.Name, trip))
        assertTrue(resultsChecker.matchesInput("name", TripAutoCompleteField.Name, trip))
        assertTrue(resultsChecker.matchesInput("c", TripAutoCompleteField.CostCenter, trip))
        assertTrue(resultsChecker.matchesInput("co", TripAutoCompleteField.CostCenter, trip))
        assertTrue(resultsChecker.matchesInput("cos", TripAutoCompleteField.CostCenter, trip))
        assertTrue(resultsChecker.matchesInput("cost", TripAutoCompleteField.CostCenter, trip))

        assertTrue(resultsChecker.matchesInput("c", TripAutoCompleteField.Comment, trip))
        assertTrue(resultsChecker.matchesInput("co", TripAutoCompleteField.Comment, trip))
        assertTrue(resultsChecker.matchesInput("com", TripAutoCompleteField.Comment, trip))
        assertTrue(resultsChecker.matchesInput("comm", TripAutoCompleteField.Comment, trip))
        assertTrue(resultsChecker.matchesInput("comme", TripAutoCompleteField.Comment, trip))
        assertTrue(resultsChecker.matchesInput("commen", TripAutoCompleteField.Comment, trip))
        assertTrue(resultsChecker.matchesInput("comment", TripAutoCompleteField.Comment, trip))

        assertFalse(resultsChecker.matchesInput("cost", TripAutoCompleteField.Name, trip))
        assertFalse(resultsChecker.matchesInput("name", TripAutoCompleteField.CostCenter, trip))
        assertFalse(resultsChecker.matchesInput("name", mock(AutoCompleteField::class.java), trip))
        assertFalse(resultsChecker.matchesInput("cost", mock(AutoCompleteField::class.java), trip))
    }

    @Test
    fun getValue() {
        assertEquals(NAME, resultsChecker.getValue(TripAutoCompleteField.Name, trip))
        assertEquals(COMMENT, resultsChecker.getValue(TripAutoCompleteField.Comment, trip))
        assertEquals(COST, resultsChecker.getValue(TripAutoCompleteField.CostCenter, trip))
    }

    @Test(expected = IllegalArgumentException::class)
    fun getValueForUnknownField() {
        resultsChecker.getValue(mock(AutoCompleteField::class.java), trip)
    }
}
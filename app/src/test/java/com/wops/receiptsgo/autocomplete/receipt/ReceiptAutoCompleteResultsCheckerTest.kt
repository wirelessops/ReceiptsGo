package com.wops.receiptsgo.autocomplete.receipt

import com.wops.receiptsgo.autocomplete.AutoCompleteField
import com.wops.receiptsgo.model.Receipt
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReceiptAutoCompleteResultsCheckerTest {

    companion object {
        private const val NAME = "name"
        private const val COMMENT = "comment"
    }

    private val resultsChecker: ReceiptAutoCompleteResultsChecker = ReceiptAutoCompleteResultsChecker()

    @Mock
    private lateinit var receipt: Receipt

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(receipt.name).thenReturn(NAME)
        whenever(receipt.comment).thenReturn(COMMENT)
    }

    @Test
    fun matchesInput() {
        assertTrue(resultsChecker.matchesInput("n", ReceiptAutoCompleteField.Name, receipt))
        assertTrue(resultsChecker.matchesInput("na", ReceiptAutoCompleteField.Name, receipt))
        assertTrue(resultsChecker.matchesInput("nam", ReceiptAutoCompleteField.Name, receipt))
        assertTrue(resultsChecker.matchesInput("name", ReceiptAutoCompleteField.Name, receipt))
        assertTrue(resultsChecker.matchesInput("c", ReceiptAutoCompleteField.Comment, receipt))
        assertTrue(resultsChecker.matchesInput("co", ReceiptAutoCompleteField.Comment, receipt))
        assertTrue(resultsChecker.matchesInput("com", ReceiptAutoCompleteField.Comment, receipt))
        assertTrue(resultsChecker.matchesInput("comm", ReceiptAutoCompleteField.Comment, receipt))
        assertTrue(resultsChecker.matchesInput("comme", ReceiptAutoCompleteField.Comment, receipt))
        assertTrue(resultsChecker.matchesInput("commen", ReceiptAutoCompleteField.Comment, receipt))
        assertTrue(resultsChecker.matchesInput("comment", ReceiptAutoCompleteField.Comment, receipt))

        assertFalse(resultsChecker.matchesInput("comment", ReceiptAutoCompleteField.Name, receipt))
        assertFalse(resultsChecker.matchesInput("name", ReceiptAutoCompleteField.Comment, receipt))
        assertFalse(resultsChecker.matchesInput("name", mock(AutoCompleteField::class.java), receipt))
        assertFalse(resultsChecker.matchesInput("comment", mock(AutoCompleteField::class.java), receipt))
    }

    @Test
    fun getValue() {
        assertEquals(NAME, resultsChecker.getValue(ReceiptAutoCompleteField.Name, receipt))
        assertEquals(COMMENT, resultsChecker.getValue(ReceiptAutoCompleteField.Comment, receipt))
    }

    @Test(expected = IllegalArgumentException::class)
    fun getValueForUnknownField() {
        resultsChecker.getValue(mock(AutoCompleteField::class.java), receipt)
    }
}
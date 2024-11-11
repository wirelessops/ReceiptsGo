package com.wops.receiptsgo.autocomplete

import com.wops.receiptsgo.persistence.database.controllers.TableController
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AutoCompleteInteractorTest {

    // Class under test
    private lateinit var interactor: AutoCompleteInteractor<Any>

    @Mock
    private lateinit var provider: AutoCompletionProvider<Any>

    @Mock
    private lateinit var resultsChecker: AutoCompleteResultsChecker<Any>

    @Mock
    private lateinit var userPreferenceManager: UserPreferenceManager

    @Mock
    private lateinit var autoCompleteField: AutoCompleteField

    @Mock
    private lateinit var tableController: TableController<Any>

    @Mock
    private lateinit var matchingResult1: Any

    @Mock
    private lateinit var matchingResult2: Any

    @Mock
    private lateinit var nonMatchingResult: Any

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(provider.tableController).thenReturn(tableController)
        whenever(tableController.get()).thenReturn(Single.just(listOf(matchingResult1, matchingResult2, nonMatchingResult)))
        whenever(userPreferenceManager[UserPreference.Receipts.EnableAutoCompleteSuggestions]).thenReturn(true)
        whenever(resultsChecker.matchesInput(any(), eq(autoCompleteField), eq(matchingResult1))).thenReturn(true)
        whenever(resultsChecker.matchesInput(any(), eq(autoCompleteField), eq(matchingResult2))).thenReturn(true)
        whenever(resultsChecker.matchesInput(any(), eq(autoCompleteField), eq(nonMatchingResult))).thenReturn(false)
        interactor = AutoCompleteInteractor(provider, resultsChecker, userPreferenceManager, Schedulers.trampoline())
    }

    @Test
    fun getAutoCompleteResultsWhenPreferenceIsDisabled() {
        whenever(userPreferenceManager[UserPreference.Receipts.EnableAutoCompleteSuggestions]).thenReturn(false)
        interactor.getAutoCompleteResults(autoCompleteField, "T")
                .test()
                .assertNoValues()
                .assertNoErrors()
                .assertComplete()
    }

    @Test
    fun getAutoCompleteResultsWhenNoCharactersAreTyped() {
        interactor.getAutoCompleteResults(autoCompleteField, "")
                .test()
                .assertNoValues()
                .assertNoErrors()
                .assertComplete()
    }

    @Test
    fun getAutoCompleteResultsWhenMultipleCharactersAreTyped() {
        whenever(resultsChecker.getValue(autoCompleteField, matchingResult1)).thenReturn("Test")
        whenever(resultsChecker.getValue(autoCompleteField, matchingResult2)).thenReturn("Test2")
        whenever(resultsChecker.getValue(autoCompleteField, nonMatchingResult)).thenReturn("Non-Test")
        interactor.getAutoCompleteResults(autoCompleteField, "Te")
                .test()
                .assertValues(mutableListOf(AutoCompleteResult("Test", matchingResult1), AutoCompleteResult("Test2", matchingResult2)))
                .assertNoErrors()
                .assertComplete()
    }

    @Test
    fun getAutoCompleteResultsWhenASingleCharacterIsTypedAndResultsHaveDifferentNames() {
        whenever(resultsChecker.getValue(autoCompleteField, matchingResult1)).thenReturn("Test")
        whenever(resultsChecker.getValue(autoCompleteField, matchingResult2)).thenReturn("Test2")
        interactor.getAutoCompleteResults(autoCompleteField, "T")
                .test()
                .assertValues(mutableListOf(AutoCompleteResult("Test", matchingResult1), AutoCompleteResult("Test2", matchingResult2)))
                .assertNoErrors()
                .assertComplete()
    }

    @Test
    fun getAutoCompleteResultsWhenASingleCharacterIsTypedButResultsHaveTheSameNames() {
        whenever(resultsChecker.getValue(autoCompleteField, matchingResult1)).thenReturn("Test")
        whenever(resultsChecker.getValue(autoCompleteField, matchingResult2)).thenReturn("Test")
        interactor.getAutoCompleteResults(autoCompleteField, "T")
                .test()
                .assertValues(mutableListOf(AutoCompleteResult("Test", matchingResult1, mutableListOf(matchingResult2))))
                .assertNoErrors()
                .assertComplete()
    }
}
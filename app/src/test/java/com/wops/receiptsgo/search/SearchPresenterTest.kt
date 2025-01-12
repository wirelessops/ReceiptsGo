package com.wops.receiptsgo.search

import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.Trip
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchPresenterTest {

    // Class under test
    private lateinit var presenter: SearchPresenter

    private val view = mock<SearchView>()
    private val interactor = mock<SearchInteractor>()

    private val inputSubject = PublishSubject.create<CharSequence>()

    @Before
    fun setUp() {
        whenever(view.inputChanges).thenReturn(inputSubject)

        presenter = SearchPresenter(view, interactor, Schedulers.trampoline(), Schedulers.trampoline())
    }

    @Test
    fun emptyInputTest() {
        presenter.subscribe()

        inputSubject.onNext("")
        inputSubject.onNext("  ")


        verify(interactor, never()).getSearchResults(any())
        verify(view, never()).presentSearchResults(any())
    }

    @Test
    fun noResultsTest() {
        whenever(interactor.getSearchResults("test"))
            .thenReturn(Single.just(SearchInteractor.SearchResults(emptyList(), emptyList())))

        presenter.subscribe()

        inputSubject.onNext("test")

        verify(interactor).getSearchResults("test")
        verify(view).presentSearchResults(SearchInteractor.SearchResults(emptyList(), emptyList()))
    }

    @Test
    fun searchResultsTest() {

        val receipt1 = mock<Receipt>()
        val receipt2 = mock<Receipt>()
        val trip = mock<Trip>()

        whenever(interactor.getSearchResults("test"))
            .thenReturn(Single.just(SearchInteractor.SearchResults(listOf(trip), listOf(receipt1, receipt2))))
        whenever(interactor.getSearchResults("test2"))
            .thenReturn(Single.just(SearchInteractor.SearchResults(emptyList(), listOf(receipt1))))

        presenter.subscribe()

        inputSubject.onNext("test")

        verify(interactor).getSearchResults("test")
        verify(view).presentSearchResults(SearchInteractor.SearchResults(listOf(trip), listOf(receipt1, receipt2)))


        inputSubject.onNext("test2")

        verify(interactor).getSearchResults("test2")
        verify(view).presentSearchResults(SearchInteractor.SearchResults(emptyList(), listOf(receipt1)))

    }
}
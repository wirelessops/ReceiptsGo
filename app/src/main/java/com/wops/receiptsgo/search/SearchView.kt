package com.wops.receiptsgo.search

import io.reactivex.Observable

interface SearchView {

    val inputChanges: Observable<CharSequence>

    fun presentSearchResults(searchResults: SearchInteractor.SearchResults)
}
package com.wops.receiptsgo.search

interface SearchResultKeeper {

    fun getSearchResult(): Searchable?

    fun markSearchResultAsProcessed()
}
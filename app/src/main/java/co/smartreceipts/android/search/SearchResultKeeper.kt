package co.smartreceipts.android.search

interface SearchResultKeeper {

    fun getSearchResult(): Searchable?

    fun markSearchResultAsProcessed()
}
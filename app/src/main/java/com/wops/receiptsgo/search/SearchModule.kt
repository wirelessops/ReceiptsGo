package com.wops.receiptsgo.search

import dagger.Binds
import dagger.Module

@Module
abstract class SearchModule {

    @Binds
    internal abstract fun provideSearchView(activity: SearchActivity): SearchView

}
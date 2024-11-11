package com.wops.receiptsgo.di

import co.smartreceipts.core.di.scopes.ApplicationScope
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Module
class ExecutorsModule {

    @Provides
    @ApplicationScope
    fun providesCachedExecutorThreadPool() : Executor {
        return Executors.newCachedThreadPool()
    }
}
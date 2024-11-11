package com.wops.receiptsgo.di

import com.wops.receiptsgo.utils.rx.RxSchedulers
import co.smartreceipts.core.di.scopes.ApplicationScope
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Named

@Module
class RxModule {

    @Provides
    @ApplicationScope
    @Named(RxSchedulers.IO)
    fun providesScheduler() : Scheduler {
        return Schedulers.io()
    }

    @Provides
    @ApplicationScope
    @Named(RxSchedulers.COMPUTATION)
    fun providesComputationScheduler() : Scheduler {
        return Schedulers.computation()
    }

    @Provides
    @ApplicationScope
    @Named(RxSchedulers.MAIN)
    fun providesMainThreadScheduler() : Scheduler {
        return AndroidSchedulers.mainThread()
    }
}
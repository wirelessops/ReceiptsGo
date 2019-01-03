package co.smartreceipts.android.di

import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.utils.rx.RxSchedulers
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Named

@Module
class ExecutorsModule {

    @Provides
    @ApplicationScope
    fun providesCachedExecutorThreadPool() : Executor {
        return Executors.newCachedThreadPool()
    }
}
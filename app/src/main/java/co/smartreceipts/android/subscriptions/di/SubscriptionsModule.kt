package co.smartreceipts.android.subscriptions.di

import co.smartreceipts.android.subscriptions.SubscriptionsActivity
import co.smartreceipts.android.subscriptions.SubscriptionsView
import dagger.Binds
import dagger.Module

@Module
abstract class SubscriptionsModule {

    @Binds
    internal abstract fun provideSubscriptionsView(activity: SubscriptionsActivity): SubscriptionsView

}

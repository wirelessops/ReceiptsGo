package com.wops.receiptsgo.subscriptions.di

import com.wops.receiptsgo.subscriptions.SubscriptionsActivity
import com.wops.receiptsgo.subscriptions.SubscriptionsView
import dagger.Binds
import dagger.Module

@Module
abstract class SubscriptionsModule {

    @Binds
    internal abstract fun provideSubscriptionsView(activity: SubscriptionsActivity): SubscriptionsView

}

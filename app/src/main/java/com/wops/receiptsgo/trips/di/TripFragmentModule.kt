package com.wops.receiptsgo.trips.di

import com.wops.receiptsgo.tooltip.TooltipView
import com.wops.receiptsgo.trips.TripFragment
import com.wops.receiptsgo.trips.navigation.ViewReceiptsInTripRouter
import dagger.Binds
import dagger.Module

@Module
abstract class TripFragmentModule {

    @Binds
    internal abstract fun provideTooltipView(fragment: TripFragment): TooltipView

    @Binds
    internal abstract fun provideViewReceiptsInTripRouter(fragment: TripFragment): ViewReceiptsInTripRouter
}

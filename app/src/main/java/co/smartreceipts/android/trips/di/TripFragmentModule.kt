package co.smartreceipts.android.trips.di

import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.trips.TripFragment
import co.smartreceipts.android.trips.navigation.ViewReceiptsInTripRouter
import dagger.Binds
import dagger.Module

@Module
abstract class TripFragmentModule {

    @Binds
    internal abstract fun provideTooltipView(fragment: TripFragment): TooltipView

    @Binds
    internal abstract fun provideViewReceiptsInTripRouter(fragment: TripFragment): ViewReceiptsInTripRouter
}

package co.smartreceipts.android.ocr.widget.tooltip.di

import co.smartreceipts.android.ocr.widget.tooltip.ReceiptCreateEditFragmentTooltipFragment
import co.smartreceipts.android.tooltip.TooltipView
import dagger.Binds
import dagger.Module

@Module
abstract class ReceiptCreateEditFragmentTooltipFragmentModule {

    @Binds
    internal abstract fun provideTooltipView(fragment: ReceiptCreateEditFragmentTooltipFragment): TooltipView

}
package com.wops.receiptsgo.ocr.widget.tooltip.di

import com.wops.receiptsgo.ocr.widget.tooltip.ReceiptCreateEditFragmentTooltipFragment
import com.wops.receiptsgo.tooltip.TooltipView
import dagger.Binds
import dagger.Module

@Module
abstract class ReceiptCreateEditFragmentTooltipFragmentModule {

    @Binds
    internal abstract fun provideTooltipView(fragment: ReceiptCreateEditFragmentTooltipFragment): TooltipView

}
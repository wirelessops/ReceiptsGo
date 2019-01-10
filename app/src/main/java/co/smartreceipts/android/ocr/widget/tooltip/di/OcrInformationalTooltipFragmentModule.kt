package co.smartreceipts.android.ocr.widget.tooltip.di

import co.smartreceipts.android.ocr.widget.tooltip.OcrInformationalTooltipFragment
import co.smartreceipts.android.tooltip.TooltipView
import dagger.Binds
import dagger.Module

@Module
abstract class OcrInformationalTooltipFragmentModule {

    @Binds
    internal abstract fun provideTooltipView(fragment: OcrInformationalTooltipFragment): TooltipView

}
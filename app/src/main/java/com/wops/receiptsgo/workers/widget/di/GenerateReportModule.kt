package com.wops.receiptsgo.workers.widget.di

import com.wops.receiptsgo.workers.widget.GenerateReportFragment
import com.wops.receiptsgo.workers.widget.GenerateReportView
import dagger.Binds
import dagger.Module

@Module
abstract class GenerateReportModule {

    @Binds
    internal abstract fun provideGenerateReportView(fragment: GenerateReportFragment): GenerateReportView

}

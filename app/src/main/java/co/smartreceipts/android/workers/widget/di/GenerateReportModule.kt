package co.smartreceipts.android.workers.widget.di

import co.smartreceipts.android.workers.widget.GenerateReportFragment
import co.smartreceipts.android.workers.widget.GenerateReportView
import dagger.Binds
import dagger.Module

@Module
abstract class GenerateReportModule {

    @Binds
    internal abstract fun provideGenerateReportView(fragment: GenerateReportFragment): GenerateReportView

}

package co.smartreceipts.android.images.di

import co.smartreceipts.android.images.CropImageActivity
import co.smartreceipts.android.images.CropView
import dagger.Binds
import dagger.Module

@Module
abstract class CropModule {

    @Binds
    internal abstract fun provideCropView(activity: CropImageActivity): CropView

}

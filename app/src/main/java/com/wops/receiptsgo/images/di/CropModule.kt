package com.wops.receiptsgo.images.di

import com.wops.receiptsgo.images.CropImageActivity
import com.wops.receiptsgo.images.CropView
import dagger.Binds
import dagger.Module

@Module
abstract class CropModule {

    @Binds
    internal abstract fun provideCropView(activity: CropImageActivity): CropView

}

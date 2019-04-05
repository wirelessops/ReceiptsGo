package co.smartreceipts.android.distance.editor.di

import co.smartreceipts.android.autocomplete.AutoCompleteView
import co.smartreceipts.android.distance.editor.DistanceCreateEditFragment
import co.smartreceipts.android.distance.editor.DistanceCreateEditView
import co.smartreceipts.android.editor.Editor
import co.smartreceipts.android.model.Distance
import dagger.Binds
import dagger.Module

@Module
abstract class DistanceCreateEditFragmentModule {

    @Binds
    internal abstract fun providesEditor(fragment: DistanceCreateEditFragment): Editor<Distance>

    @Binds
    internal abstract fun providesReceiptAutoCompleteView(fragment: DistanceCreateEditFragment): AutoCompleteView<Distance>

    @Binds
    internal abstract fun provideDistanceCreateEditView(fragment: DistanceCreateEditFragment): DistanceCreateEditView

}

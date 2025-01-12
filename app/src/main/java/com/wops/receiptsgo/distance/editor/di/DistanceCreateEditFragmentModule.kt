package com.wops.receiptsgo.distance.editor.di

import com.wops.receiptsgo.autocomplete.AutoCompleteView
import com.wops.receiptsgo.distance.editor.DistanceCreateEditFragment
import com.wops.receiptsgo.distance.editor.DistanceCreateEditView
import com.wops.receiptsgo.editor.Editor
import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.receipts.editor.paymentmethods.PaymentMethodsView
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

    @Binds
    internal abstract fun providesPaymentMethodsView(fragment: DistanceCreateEditFragment): PaymentMethodsView

}

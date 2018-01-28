package co.smartreceipts.android.receipts.editor.di;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.receipts.editor.ReceiptCreateEditFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent(modules = { ReceiptsCreateEditModule.class })
public interface ReceiptsCreateEditFragmentSubcomponent extends AndroidInjector<ReceiptCreateEditFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ReceiptCreateEditFragment> {

    }
}

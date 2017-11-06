package co.smartreceipts.android.receipts.editor.di;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.receipts.editor.CreateEditReceiptFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent(modules = { ReceiptsCreateEditModule.class })
public interface ReceiptsCreateEditFragmentSubcomponent extends AndroidInjector<CreateEditReceiptFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<CreateEditReceiptFragment> {

    }
}

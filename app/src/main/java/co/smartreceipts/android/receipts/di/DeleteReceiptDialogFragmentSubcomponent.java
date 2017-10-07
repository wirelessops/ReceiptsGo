package co.smartreceipts.android.receipts.di;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.receipts.delete.DeleteReceiptDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface DeleteReceiptDialogFragmentSubcomponent extends AndroidInjector<DeleteReceiptDialogFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<DeleteReceiptDialogFragment> {

    }
}

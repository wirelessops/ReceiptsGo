package co.smartreceipts.android.receipts.editor.di;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.receipts.editor.CreateEditFragmentListEditor;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent(modules = { ReceiptsCreateEditModule.class })
public interface ReceiptsCreateEditFragmentSubcomponent extends AndroidInjector<CreateEditFragmentListEditor> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<CreateEditFragmentListEditor> {

    }
}

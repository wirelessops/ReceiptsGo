package co.smartreceipts.android.receipts.di;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.identity.widget.di.LoginModule;
import co.smartreceipts.android.receipts.ReceiptsListFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent(modules = { ReceiptsListModule.class })
public interface ReceiptsListFragmentSubcomponent extends AndroidInjector<ReceiptsListFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ReceiptsListFragment> {

    }
}

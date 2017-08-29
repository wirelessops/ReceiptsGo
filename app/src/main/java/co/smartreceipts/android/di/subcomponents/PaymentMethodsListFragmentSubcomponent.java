package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.settings.widget.editors.PaymentMethodsListFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface PaymentMethodsListFragmentSubcomponent extends AndroidInjector<PaymentMethodsListFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<PaymentMethodsListFragment> {

    }
}

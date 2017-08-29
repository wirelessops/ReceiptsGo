package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.settings.widget.editors.CategoriesListFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface CategoriesListFragmentSubcomponent extends AndroidInjector<CategoriesListFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<CategoriesListFragment> {

    }
}

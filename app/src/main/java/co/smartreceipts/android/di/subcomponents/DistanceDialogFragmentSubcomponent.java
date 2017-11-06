package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.distance.editor.DistanceDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface DistanceDialogFragmentSubcomponent extends AndroidInjector<DistanceDialogFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<DistanceDialogFragment> {
    }
}

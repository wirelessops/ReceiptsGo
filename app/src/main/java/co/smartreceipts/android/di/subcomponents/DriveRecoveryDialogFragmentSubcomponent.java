package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.sync.widget.errors.DriveRecoveryDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface DriveRecoveryDialogFragmentSubcomponent extends AndroidInjector<DriveRecoveryDialogFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<DriveRecoveryDialogFragment> {

    }
}

package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.sync.widget.backups.AutomaticBackupsInfoDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface AutomaticBackupsInfoDialogFragmentSubcomponent extends AndroidInjector<AutomaticBackupsInfoDialogFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<AutomaticBackupsInfoDialogFragment> {
    }
}

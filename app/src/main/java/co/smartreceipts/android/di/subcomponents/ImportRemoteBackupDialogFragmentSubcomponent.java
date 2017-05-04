package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.sync.widget.backups.ImportRemoteBackupDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface ImportRemoteBackupDialogFragmentSubcomponent extends AndroidInjector<ImportRemoteBackupDialogFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ImportRemoteBackupDialogFragment> {

    }
}

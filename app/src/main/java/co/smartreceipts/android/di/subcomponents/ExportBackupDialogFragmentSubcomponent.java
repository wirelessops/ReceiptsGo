package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.sync.widget.backups.ExportBackupDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface ExportBackupDialogFragmentSubcomponent extends AndroidInjector<ExportBackupDialogFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ExportBackupDialogFragment> {

    }
}

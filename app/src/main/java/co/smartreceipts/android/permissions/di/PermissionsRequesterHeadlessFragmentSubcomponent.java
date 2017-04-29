package co.smartreceipts.android.permissions.di;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.permissions.PermissionRequesterHeadlessFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface PermissionsRequesterHeadlessFragmentSubcomponent extends AndroidInjector<PermissionRequesterHeadlessFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<PermissionRequesterHeadlessFragment> {
    }
}

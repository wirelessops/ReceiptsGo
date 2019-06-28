package co.smartreceipts.android.permissions;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.FragmentActivity;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Single;

@ActivityScope
public class ActivityPermissionsRequester<T extends FragmentActivity> implements PermissionRequester {

    private final HeadlessFragmentPermissionRequesterFactory permissionRequesterFactory;

    @Inject
    public ActivityPermissionsRequester(@NonNull T activity) {
        this(new HeadlessFragmentPermissionRequesterFactory(activity));
    }

    @VisibleForTesting
    public ActivityPermissionsRequester(@NonNull HeadlessFragmentPermissionRequesterFactory permissionRequesterFactory) {
        this.permissionRequesterFactory = Preconditions.checkNotNull(permissionRequesterFactory);
    }

    @NonNull
    @Override
    public Single<PermissionAuthorizationResponse> request(@NonNull String manifestPermission) {
        Logger.info(this, "Requesting permission: {}", manifestPermission);
        try {
            return permissionRequesterFactory.get().request(manifestPermission);
        } catch (IllegalStateException e) {
            return Single.error(e);
        }
    }

    @Override
    public void markRequestConsumed(@NonNull String manifestPermission) {
        Logger.info(this, "Marking permission request consumed {}", manifestPermission);

        permissionRequesterFactory.get().markRequestConsumed(manifestPermission);
    }


}

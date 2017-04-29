package co.smartreceipts.android.permissions;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.FragmentActivity;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Single;

@ActivityScope
public class ActivityPermissionsRequester<T extends FragmentActivity> implements PermissionRequester {

    private final HeadlessFragmentPermissionRequesterFactory permissionRequesterFactory;

    @Inject
    public ActivityPermissionsRequester(@NonNull T activity, @NonNull Analytics analytics) {
        this(new HeadlessFragmentPermissionRequesterFactory(activity, analytics));
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

}

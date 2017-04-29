package co.smartreceipts.android.permissions;

import android.support.annotation.NonNull;

import io.reactivex.Single;

public interface PermissionRequester {

    /**
     * Requests the end user to grant us a specific permission for usage within the app
     *
     * @param manifestPermission the permission to request {@see {@link android.Manifest.permission}}
     *
     * @return a {@link Single}, which will emit {@link io.reactivex.SingleEmitter#onSuccess(Object)}
     * with a value of {@link PermissionAuthorizationResponse}, regardless of whether or not this
     * request was successfully granted. Consumers are responsible for for interacting with the
     * {@link PermissionAuthorizationResponse} to determine which next steps should be taken. This
     * can also emit {@link io.reactivex.SingleEmitter#onError(Throwable)} if we're not currently
     * bound to the Android lifecycle in such a manner as to allow this request
     */
    @NonNull
    Single<PermissionAuthorizationResponse> request(@NonNull String manifestPermission);
}

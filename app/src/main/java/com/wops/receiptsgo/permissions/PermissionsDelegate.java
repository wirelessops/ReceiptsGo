package com.wops.receiptsgo.permissions;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import com.wops.receiptsgo.activities.SmartReceiptsActivity;
import com.wops.receiptsgo.permissions.exceptions.PermissionsNotGrantedException;
import co.smartreceipts.core.di.scopes.ActivityScope;
import io.reactivex.Completable;

@ActivityScope
public class PermissionsDelegate {

    @Inject
    PermissionStatusChecker permissionStatusChecker;
    @Inject
    ActivityPermissionsRequester<SmartReceiptsActivity> permissionRequester;

    @Inject
    public PermissionsDelegate() {
    }

    public Completable checkPermissionAndMaybeAsk(@NonNull String manifestPermission) {
        return permissionStatusChecker.isPermissionGranted(manifestPermission)
                .flatMapCompletable(isGranted -> {
                    if (isGranted) {
                        return Completable.complete();
                    } else {
                        return permissionRequester.request(manifestPermission)
                                .flatMapCompletable(permissionResponse -> {
                                    if (permissionResponse.wasGranted()) {
                                        return Completable.complete();
                                    } else {
                                        return Completable.error(new PermissionsNotGrantedException("User failed to grant permission", manifestPermission));
                                    }
                                });
                    }
                });
    }

    public void markRequestConsumed(@NonNull String manifestPermission) {
        permissionRequester.markRequestConsumed(manifestPermission);
    }
}

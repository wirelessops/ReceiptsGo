package com.wops.receiptsgo.permissions;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import com.wops.core.di.scopes.ApplicationScope;
import io.reactivex.Single;

@ApplicationScope
public class PermissionStatusChecker {

    private final Context context;

    @Inject
    public PermissionStatusChecker(@NonNull Context context) {
        this.context = Preconditions.checkNotNull(context);
    }

    /**
     * Checks if this permission has been granted to our app
     *
     * @param manifestPermission the permission to check {@see {@link android.Manifest.permission}}
     *
     * @return a {@link Single}, which will emit {@link io.reactivex.SingleEmitter#onSuccess(Object)}
     * with a value of {@code true} if this permission is currently granted or {@code false} if not.
     */
    @NonNull
    public Single<Boolean> isPermissionGranted(@NonNull String manifestPermission) {
        return Single.just(ContextCompat.checkSelfPermission(context, manifestPermission) == PackageManager.PERMISSION_GRANTED);
    }
}

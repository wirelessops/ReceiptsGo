package co.smartreceipts.android.permissions;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

public class PermissionsNotGrantedException extends Exception {

    private final String permission;

    public PermissionsNotGrantedException(@NonNull String message, @NonNull String permission) {
        super(message);
        this.permission = Preconditions.checkNotNull(permission);
    }

    @NonNull
    public String getPermission() {
        return permission;
    }
}

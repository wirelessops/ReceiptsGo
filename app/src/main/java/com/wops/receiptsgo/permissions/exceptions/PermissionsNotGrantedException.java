package com.wops.receiptsgo.permissions.exceptions;

import androidx.annotation.NonNull;

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

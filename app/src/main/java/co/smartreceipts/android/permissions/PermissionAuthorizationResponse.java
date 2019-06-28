package co.smartreceipts.android.permissions;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

public class PermissionAuthorizationResponse {

    private final String permission;
    private final boolean isGranted;

    public PermissionAuthorizationResponse(@NonNull String permission, boolean isGranted) {
        this.permission = Preconditions.checkNotNull(permission);
        this.isGranted = isGranted;
    }

    @NonNull
    public String getPermission() {
        return permission;
    }

    public boolean wasGranted() {
        return isGranted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissionAuthorizationResponse)) return false;

        PermissionAuthorizationResponse that = (PermissionAuthorizationResponse) o;

        if (isGranted != that.isGranted) return false;
        return permission.equals(that.permission);

    }

    @Override
    public int hashCode() {
        int result = permission.hashCode();
        result = 31 * result + (isGranted ? 1 : 0);
        return result;
    }
}

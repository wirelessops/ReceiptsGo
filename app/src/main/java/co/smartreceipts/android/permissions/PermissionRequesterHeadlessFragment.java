package co.smartreceipts.android.permissions;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Single;
import io.reactivex.subjects.SingleSubject;

public class PermissionRequesterHeadlessFragment extends Fragment implements PermissionRequester {

    private static final int PERMISSION_REQUEST = 33;

    private final Map<String, SingleSubject<PermissionAuthorizationResponse>> permissionRequestMap = new HashMap<>();
    private final Object lock = new Object();

    Analytics analytics;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Preconditions.checkNotNull(analytics, "Please ensure that we're setting the analytics member variable");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST && permissions.length > 0) {

            final String permission = permissions[0];
            final SingleSubject<PermissionAuthorizationResponse> responseSubject;
            final PermissionAuthorizationResponse response;
            synchronized (lock) {
                responseSubject = Preconditions.checkNotNull(permissionRequestMap.get(permission));
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    response = new PermissionAuthorizationResponse(permission, true);
                } else {
                    response = new PermissionAuthorizationResponse(permission, false);
                }
            }
            responseSubject.onSuccess(response);

            analytics.record(new DefaultDataPointEvent(response.wasGranted() ? Events.Permissions.PermissionGranted : Events.Permissions.PermissionDenied)
                    .addDataPoint(new DataPoint("permission", permission)));

            Logger.info(this, response.wasGranted() ? "User authorized: {}." : "User did NOT authorize: {}.", response.getPermission());

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @NonNull
    @Override
    public Single<PermissionAuthorizationResponse> request(@NonNull String manifestPermission) {
        Logger.info(this, "Requesting permission: {}", manifestPermission);
        analytics.record(new DefaultDataPointEvent(Events.Permissions.PermissionRequested).addDataPoint(new DataPoint("permission", manifestPermission)));

        SingleSubject<PermissionAuthorizationResponse> single;
        boolean permissionRequestIsRequired = false;
        synchronized (lock) {
            single = permissionRequestMap.get(manifestPermission);
            if (single == null) {
                single = SingleSubject.create();
                permissionRequestMap.put(manifestPermission, single);
                permissionRequestIsRequired = true;
            }
        }
        if (permissionRequestIsRequired) {
            requestPermissions(new String[]{manifestPermission}, PERMISSION_REQUEST);
        }
        return single;
    }

    public void markRequestConsumed(@NonNull String manifestPermission) {
        Logger.info(this, "Marking permission request consumed: {}", manifestPermission);
        permissionRequestMap.remove(manifestPermission);
    }
}

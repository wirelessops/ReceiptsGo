package co.smartreceipts.android.sync.drive.device;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;

import java.util.UUID;

public class DeviceMetadata {

    private final Context mContext;

    public DeviceMetadata(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    @NonNull
    public String getUniqueDeviceId() {
        return UUID.randomUUID().toString();
    }

    @NonNull
    public String getDeviceName() {
        final String name = Build.MODEL;
        if (name != null) {
            return name;
        } else {
            return "";
        }
    }
}

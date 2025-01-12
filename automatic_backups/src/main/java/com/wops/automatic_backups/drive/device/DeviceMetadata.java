package com.wops.automatic_backups.drive.device;

import android.os.Build;

import androidx.annotation.NonNull;

import java.util.UUID;

public class DeviceMetadata {

    public DeviceMetadata() {
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

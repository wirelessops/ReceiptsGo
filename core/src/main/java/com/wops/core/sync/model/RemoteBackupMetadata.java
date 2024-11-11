package com.wops.core.sync.model;

import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.Date;

import com.wops.core.sync.model.impl.Identifier;

public interface RemoteBackupMetadata extends Parcelable {

    /**
     * @return the {@link Identifier} that represents this parent object (eg the Google Drive
     * resource id)
     */
    @NonNull
    Identifier getId();

    /**
     * @return the {@link Identifier} of the device that uploaded this backup (eg the UDID)
     */
    @NonNull
    Identifier getSyncDeviceId();

    /**
     * @return the name of the device that created this backup (e.g. Pixel 2)
     */
    @NonNull
    String getSyncDeviceName();

    /**
     * @return the {@link Date} when this backup was last modified
     */
    @NonNull
    Date getLastModifiedDate();
}

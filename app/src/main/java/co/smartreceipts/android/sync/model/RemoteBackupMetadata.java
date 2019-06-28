package co.smartreceipts.android.sync.model;

import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.Date;

import co.smartreceipts.android.sync.model.impl.Identifier;

public interface RemoteBackupMetadata extends Parcelable {

    @NonNull
    Identifier getId();

    @NonNull
    Identifier getSyncDeviceId();

    @NonNull
    String getSyncDeviceName();

    @NonNull
    Date getLastModifiedDate();
}

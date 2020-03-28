package co.smartreceipts.core.sync.model.impl;

import android.os.Parcel;
import androidx.annotation.NonNull;

import com.google.api.client.util.DateTime;
import com.google.common.base.Preconditions;

import java.util.Date;

import co.smartreceipts.core.sync.model.RemoteBackupMetadata;

public class DefaultRemoteBackupMetadata implements RemoteBackupMetadata {

    private final Identifier mIdentifier;
    private final Identifier mSyncDeviceIdentifier;
    private final String mSyncDeviceName;
    private final DateTime mLastModifiedDate;

    public DefaultRemoteBackupMetadata(@NonNull Identifier identifier, @NonNull Identifier syncDeviceIdentifier, @NonNull String syncDeviceName,
                                       @NonNull DateTime lastModifiedDate) {
        mIdentifier = Preconditions.checkNotNull(identifier);
        mSyncDeviceIdentifier = Preconditions.checkNotNull(syncDeviceIdentifier);
        mSyncDeviceName = Preconditions.checkNotNull(syncDeviceName);
        mLastModifiedDate = Preconditions.checkNotNull(lastModifiedDate);
    }

    @NonNull
    @Override
    public Identifier getId() {
        return mIdentifier;
    }

    @NonNull
    @Override
    public Identifier getSyncDeviceId() {
        return mSyncDeviceIdentifier;
    }

    @NonNull
    @Override
    public String getSyncDeviceName() {
        return mSyncDeviceName;
    }

    @NonNull
    @Override
    public Date getLastModifiedDate() {
        return new Date(mLastModifiedDate.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultRemoteBackupMetadata)) return false;

        DefaultRemoteBackupMetadata that = (DefaultRemoteBackupMetadata) o;

        if (!mIdentifier.equals(that.mIdentifier)) return false;
        if (!mSyncDeviceIdentifier.equals(that.mSyncDeviceIdentifier)) return false;
        if (!mSyncDeviceName.equals(that.mSyncDeviceName)) return false;
        return mLastModifiedDate.equals(that.mLastModifiedDate);

    }

    @Override
    public int hashCode() {
        int result = mIdentifier.hashCode();
        result = 31 * result + mSyncDeviceIdentifier.hashCode();
        result = 31 * result + mSyncDeviceName.hashCode();
        result = 31 * result + mLastModifiedDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DefaultRemoteBackupMetadata{" +
                "mIdentifier=" + mIdentifier +
                ", mSyncDeviceIdentifier=" + mSyncDeviceIdentifier +
                ", mSyncDeviceName='" + mSyncDeviceName + '\'' +
                ", mLastModifiedDate=" + mLastModifiedDate +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mIdentifier, flags);
        dest.writeParcelable(this.mSyncDeviceIdentifier, flags);
        dest.writeString(this.mSyncDeviceName);
        dest.writeLong(this.mLastModifiedDate.getValue());
    }

    public static final Creator<DefaultRemoteBackupMetadata> CREATOR = new Creator<DefaultRemoteBackupMetadata>() {
        @Override
        public DefaultRemoteBackupMetadata createFromParcel(@NonNull Parcel in) {
            return new DefaultRemoteBackupMetadata((Identifier) in.readParcelable(Identifier.class.getClassLoader()),
                    (Identifier) in.readParcelable(Identifier.class.getClassLoader()), in.readString(), new DateTime(in.readLong()));
        }

        @Override
        public DefaultRemoteBackupMetadata[] newArray(int size) {
            return new DefaultRemoteBackupMetadata[size];
        }
    };
}

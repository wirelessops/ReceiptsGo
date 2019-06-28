package co.smartreceipts.android.sync.drive.services;

import androidx.annotation.NonNull;

import com.google.android.gms.drive.DriveId;
import com.google.common.base.Preconditions;

public class DriveIdUploadMetadata {

    private final DriveId driveId;
    private final String trackingTag;

    public DriveIdUploadMetadata(@NonNull DriveId driveId, @NonNull String trackingTag) {
        this.driveId = Preconditions.checkNotNull(driveId);
        this.trackingTag = Preconditions.checkNotNull(trackingTag);
    }

    @NonNull
    public DriveId getDriveId() {
        return driveId;
    }

    @NonNull
    public String getTrackingTag() {
        return trackingTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriveIdUploadMetadata)) return false;

        DriveIdUploadMetadata that = (DriveIdUploadMetadata) o;

        if (!driveId.equals(that.driveId)) return false;
        return trackingTag.equals(that.trackingTag);

    }

    @Override
    public int hashCode() {
        int result = driveId.hashCode();
        result = 31 * result + trackingTag.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DriveIdUploadMetadata{" +
                "driveId=" + driveId +
                ", trackingTag='" + trackingTag + '\'' +
                '}';
    }
}

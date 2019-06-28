package co.smartreceipts.android.sync.drive.services;

import androidx.annotation.NonNull;

import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.events.CompletionEvent;
import com.google.common.base.Preconditions;

import java.util.List;

/**
 * A simple wrapper around a {@link CompletionEvent} to allow us to unit test this behavior (as it's
 * a final class)
 */
class DriveCompletionEventWrapper {

    private final CompletionEvent completionEvent;

    public DriveCompletionEventWrapper(@NonNull CompletionEvent completionEvent) {
        this.completionEvent = Preconditions.checkNotNull(completionEvent);
    }

    public DriveId getDriveId() {
        return this.completionEvent.getDriveId();
    }

    public List<String> getTrackingTags() {
        return this.completionEvent.getTrackingTags();
    }

    public int getStatus() {
        return this.completionEvent.getStatus();
    }

    public void dismiss() {
        this.completionEvent.dismiss();
    }
}

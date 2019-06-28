package co.smartreceipts.android.sync.drive.services;

import androidx.annotation.NonNull;

import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.events.CompletionEvent;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.utils.log.Logger;

/**
 * Drive's APIs are not RESTful, so we can get the event resource id at any time after an upload completes.
 * Usually if we have network, it's pretty quick. Additionally, Google tied us to using a manifest-registered
 * service for this stuff, so we really have nothing in the means of dynamic controls for this :(... But the
 * good news is, we can use Dagger to easily inject this class and effectively remove our reliance on their
 * service definition.
 * <p>
 * This class services as a more "OO-friendly" wrapper that we can use for unit testing purposes
 * </p>
 */
@ApplicationScope
public class DriveUploadCompleteManager {

    @Inject
    Analytics analytics;

    private final Object lock = new Object();
    private final Map<DriveIdUploadMetadata, DriveIdUploadCompleteCallback> metadataToCallbackMap = new HashMap<>();
    private final Map<DriveId, DriveIdUploadMetadata> driveIdToMetadataMap = new HashMap<>();
    private final Map<String, DriveIdUploadMetadata> trackingTagToMetadataMap = new HashMap<>();

    @Inject
    public DriveUploadCompleteManager(@NonNull Analytics analytics) {
        this.analytics = Preconditions.checkNotNull(analytics);
    }

    /**
     * Should be triggered via the {@link DriveCompletionEventService#onCompletion(CompletionEvent)} method to
     * allow us to perform our processing outside of our service (and hence allow for easier injection).
     * <p>
     * This has been intentionally made "package-private" in order to limit the degree of interaction
     * </p>
     * @param event a {@link DriveCompletionEventWrapper} which contains the underlying event
     */
    void onCompletion(@NonNull DriveCompletionEventWrapper event) {
        final DriveIdUploadCompleteCallback callback;
        final DriveIdUploadMetadata foundMetadata;
        synchronized (lock) {
            final DriveIdUploadMetadata metadataFromDriveId = driveIdToMetadataMap.remove(event.getDriveId());
            if (metadataFromDriveId != null) {
                // If we were able to find the metadata via the event's drive id, use that for the callback (and clear the rest)
                foundMetadata = metadataFromDriveId;
                callback = metadataToCallbackMap.remove(metadataFromDriveId);
                trackingTagToMetadataMap.remove(metadataFromDriveId.getTrackingTag());
            } else {
                // Otherwise, let's see if we can grab it from the tracking tags (and then clear the rest)
                final List<String> intersection = new ArrayList<>(trackingTagToMetadataMap.keySet());
                intersection.retainAll(event.getTrackingTags());
                if (intersection.size() == 1) {
                    final DriveIdUploadMetadata metadataFromTrackingTag = trackingTagToMetadataMap.remove(intersection.get(0));
                    foundMetadata = metadataFromTrackingTag;
                    callback = metadataToCallbackMap.remove(metadataFromTrackingTag);
                    driveIdToMetadataMap.remove(metadataFromTrackingTag.getDriveId());
                } else {
                    foundMetadata = null;
                    callback = null;
                }
            }
        }

        // Note: doing this outside our lock
        if (callback != null) {
            if (event.getStatus() == CompletionEvent.STATUS_SUCCESS) {
                Logger.info(DriveCompletionEventService.class, "Calling back with persisted drive id {} with resource id: {}. Source: {}.", event.getDriveId(), event.getDriveId().getResourceId(), foundMetadata);
                analytics.record(Events.Sync.DriveCompletionEventHandledWithSuccess);
                callback.onSuccess(event.getDriveId());
            } else {
                Logger.error(DriveCompletionEventService.class, "Calling back with drive resource id failure. Source: {}.", foundMetadata);
                analytics.record(Events.Sync.DriveCompletionEventHandledWithFailure);
                callback.onFailure(event.getDriveId());
            }
        } else {
            Logger.warn(DriveCompletionEventService.class, "Received an event before a callback was registered. Source: {}.", foundMetadata);
            analytics.record(Events.Sync.DriveCompletionEventNotHandled);
        }

        event.dismiss();
    }

    /**
     * Registers a callback to inform us when a specific drive id has a valid resource id, so we can persist
     * this as having fully completed
     *
     * @param driveIdUploadMetadata - the metadata corresponding to this upload
     * @param callback - the callback to inform us when this has occurred
     */
    public void registerCallback(@NonNull DriveIdUploadMetadata driveIdUploadMetadata, @NonNull DriveIdUploadCompleteCallback callback) {
        Preconditions.checkNotNull(driveIdUploadMetadata);
        Preconditions.checkNotNull(callback);

        synchronized (lock) {
            metadataToCallbackMap.put(driveIdUploadMetadata, callback);
            driveIdToMetadataMap.put(driveIdUploadMetadata.getDriveId(), driveIdUploadMetadata);
            trackingTagToMetadataMap.put(driveIdUploadMetadata.getTrackingTag(), driveIdUploadMetadata);
        }

        Logger.info(DriveCompletionEventService.class, "Registered for completion of id: {}", driveIdUploadMetadata);
    }
}

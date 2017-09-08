package co.smartreceipts.android.sync.drive.services;

import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.events.CompletionEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;

import co.smartreceipts.android.analytics.Analytics;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DriveUploadCompleteManagerTest {

    private static final String TRACKING_TAG = "tracking_tag";

    @InjectMocks
    DriveUploadCompleteManager driveUploadCompleteManager;

    @Mock
    Analytics analytics;

    @Mock
    DriveIdUploadMetadata metadata;

    @Mock
    DriveIdUploadCompleteCallback callback;

    @Mock
    DriveId driveId, driveId2;

    @Mock
    DriveCompletionEventWrapper completionEvent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(metadata.getDriveId()).thenReturn(driveId);
        when(metadata.getTrackingTag()).thenReturn(TRACKING_TAG);
        when(completionEvent.getDriveId()).thenReturn(driveId);
    }

    @Test
    public void onCompletionWithoutAnyRegisteredCallbacks() throws Exception {
        driveUploadCompleteManager.onCompletion(completionEvent);
        verifyZeroInteractions(callback);
        verify(completionEvent).dismiss();
    }

    @Test
    public void onCompletionSuccessForValidDriveId() throws Exception {
        when(completionEvent.getStatus()).thenReturn(CompletionEvent.STATUS_SUCCESS);
        driveUploadCompleteManager.registerCallback(metadata, callback);
        driveUploadCompleteManager.onCompletion(completionEvent);
        verify(callback).onSuccess(driveId);
        verify(completionEvent).dismiss();
    }

    @Test
    public void onCompletionSuccessForValidDriveIdIsOnlyHandledOnce() throws Exception {
        when(completionEvent.getStatus()).thenReturn(CompletionEvent.STATUS_SUCCESS);
        driveUploadCompleteManager.registerCallback(metadata, callback);

        // Multiple calls:
        driveUploadCompleteManager.onCompletion(completionEvent);
        driveUploadCompleteManager.onCompletion(completionEvent);
        driveUploadCompleteManager.onCompletion(completionEvent);

        verify(callback).onSuccess(driveId);
        verify(completionEvent, times(3)).dismiss();
    }

    @Test
    public void onCompletionFailureForValidDriveId() throws Exception {
        when(completionEvent.getStatus()).thenReturn(CompletionEvent.STATUS_FAILURE);
        driveUploadCompleteManager.registerCallback(metadata, callback);
        driveUploadCompleteManager.onCompletion(completionEvent);
        verify(callback).onFailure(driveId);
        verify(completionEvent).dismiss();
    }

    @Test
    public void onCompletionSuccessForValidTrackingTag() throws Exception {
        when(completionEvent.getDriveId()).thenReturn(driveId2);
        when(completionEvent.getTrackingTags()).thenReturn(Arrays.asList("tag1", TRACKING_TAG, "tag2"));
        when(completionEvent.getStatus()).thenReturn(CompletionEvent.STATUS_SUCCESS);
        driveUploadCompleteManager.registerCallback(metadata, callback);
        driveUploadCompleteManager.onCompletion(completionEvent);
        verify(callback).onSuccess(driveId2);
        verify(completionEvent).dismiss();
    }

    @Test
    public void onCompletionSuccessForValidTrackingTagIsOnlyHandledOnce() throws Exception {
        when(completionEvent.getDriveId()).thenReturn(driveId2);
        when(completionEvent.getTrackingTags()).thenReturn(Arrays.asList("tag1", TRACKING_TAG, "tag2"));
        when(completionEvent.getStatus()).thenReturn(CompletionEvent.STATUS_SUCCESS);
        driveUploadCompleteManager.registerCallback(metadata, callback);

        // Multiple calls:
        driveUploadCompleteManager.onCompletion(completionEvent);
        driveUploadCompleteManager.onCompletion(completionEvent);
        driveUploadCompleteManager.onCompletion(completionEvent);

        verify(callback).onSuccess(driveId2);
        verify(completionEvent, times(3)).dismiss();
    }

    @Test
    public void onCompletionFailureForValidTrackingTag() throws Exception {
        when(completionEvent.getDriveId()).thenReturn(driveId2);
        when(completionEvent.getTrackingTags()).thenReturn(Arrays.asList("tag1", TRACKING_TAG, "tag2"));
        when(completionEvent.getStatus()).thenReturn(CompletionEvent.STATUS_CONFLICT);
        driveUploadCompleteManager.registerCallback(metadata, callback);
        driveUploadCompleteManager.onCompletion(completionEvent);
        verify(callback).onFailure(driveId2);
        verify(completionEvent).dismiss();
    }

}
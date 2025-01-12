package com.wops.receiptsgo.imports;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.common.base.Preconditions;

import java.io.File;
import java.lang.ref.WeakReference;

import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.utils.IntentUtils;
import com.wops.receiptsgo.utils.StrictModeConfiguration;
import com.wops.receiptsgo.utils.cache.SmartReceiptsTemporaryFileCache;
import com.wops.analytics.log.Logger;

public class CameraInteractionController {

    private final Context context;
    private final WeakReference<Fragment> fragmentReference;

    public CameraInteractionController(@NonNull Fragment fragment) {
        context = Preconditions.checkNotNull(fragment.getContext()).getApplicationContext();
        fragmentReference = new WeakReference<>(Preconditions.checkNotNull(fragment));
    }

    /**
     * Takes a photo for a given trip directory
     *
     * @return the Uri result of the photo
     */
    @NonNull
    public Uri takePhoto() {
        return StrictModeConfiguration.permitDiskReads(() ->
                startPhotoIntent(new SmartReceiptsTemporaryFileCache(context).getInternalCacheFile(System.currentTimeMillis() + "x.jpg"),
                        RequestCodes.NEW_RECEIPT_CAMERA_IMAGE));
    }

    /**
     * Takes a photo for a given receipt
     *
     * @return the Uri result of the photo
     */
    @NonNull
    public Uri addPhoto() {
        return StrictModeConfiguration.permitDiskReads(() ->
                startPhotoIntent(new SmartReceiptsTemporaryFileCache(context).getInternalCacheFile(System.currentTimeMillis() + "x.jpg"),
                        RequestCodes.ATTACH_CAMERA_IMAGE));
    }

    /**
     * Retakes a photo for a given receipt
     *
     * @param receipt the desired {@link Receipt}
     * @return the Uri result of the photo
     */
    @NonNull
    public Uri retakePhoto(@NonNull Receipt receipt) {
        Preconditions.checkNotNull(receipt.getFile());
        return startPhotoIntent(receipt.getFile(), RequestCodes.RETAKE_CAMERA_IMAGE);
    }

    @NonNull
    private Uri startPhotoIntent(@NonNull File saveLocation, int nativeCameraRequestCode) {
        final Fragment fragment = fragmentReference.get();
        if (fragment == null || !fragment.isResumed()) {
            Logger.warn(this, "Returning empty URI as save location");
            return Uri.EMPTY;
        }

        final Intent intent = IntentUtils.getImageCaptureIntent(fragment.getActivity(), saveLocation);
        fragment.startActivityForResult(intent, nativeCameraRequestCode);
        final Uri uri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
        Logger.debug(this, "Returning {} as save location", uri);

        return uri;
    }
}

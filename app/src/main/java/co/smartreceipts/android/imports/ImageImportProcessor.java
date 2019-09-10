package co.smartreceipts.android.imports;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.exifinterface.media.ExifInterface;
import androidx.core.content.ContextCompat;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import co.smartreceipts.android.imports.exceptions.InvalidImageException;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Single;
import co.smartreceipts.android.utils.ImageUtils;
import wb.android.storage.StorageManager;

public class ImageImportProcessor implements FileImportProcessor {

    @VisibleForTesting
    static final int COMPRESSION_QUALITY = 95;

    private static final int MAX_DIMENSION = 1024;
    private static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    private final Trip mTrip;
    private final StorageManager mStorageManner;
    private final UserPreferenceManager mPreferences;
    private final Context mContext;
    private final ContentResolver mContentResolver;

    public ImageImportProcessor(@NonNull Trip trip, @NonNull StorageManager storageManager, @NonNull UserPreferenceManager preferences, @NonNull Context context) {
        this(trip, storageManager, preferences, context, context.getContentResolver());
    }

    public ImageImportProcessor(@NonNull Trip trip, @NonNull StorageManager storageManager, @NonNull UserPreferenceManager preferences, @NonNull Context context, @NonNull ContentResolver contentResolver) {
        mTrip = Preconditions.checkNotNull(trip);
        mStorageManner = Preconditions.checkNotNull(storageManager);
        mPreferences = Preconditions.checkNotNull(preferences);
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mContentResolver = Preconditions.checkNotNull(contentResolver);
    }

    @NonNull
    @Override
    public Single<File> process(@NonNull final Uri uri) {
        return Single.create(emitter -> {
            InputStream inputStream = null;
            try {
                inputStream = mContentResolver.openInputStream(uri);
                if (inputStream != null) {
                    final int scale = getImageScaleFactor(uri);

                    // Get scaled bitmap
                    final BitmapFactory.Options smallerOpts = new BitmapFactory.Options();
                    smallerOpts.inSampleSize = scale;
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, smallerOpts);

                    // Checking if this file is actual image
                    if (smallerOpts.outHeight <= 0 && smallerOpts.outWidth <= 0) {
                        emitter.onError(new InvalidImageException("Looks like selected file is not an image"));
                        return;
                    }

                    // Perform image processing
                    if (mPreferences.get(UserPreference.Camera.SaveImagesInGrayScale)) {
                        bitmap = ImageUtils.convertToGrayScale(bitmap);
                    }

                    if (mPreferences.get(UserPreference.Camera.AutomaticallyRotateImages)) {
                        Logger.debug(ImageImportProcessor.this, "Configured for auto-rotation. Attempting to determine the orientation");
                        int orientation = getOrientationFromMediaStore(uri);

                        if (orientation == ExifInterface.ORIENTATION_UNDEFINED) {
                            Logger.warn(ImageImportProcessor.this, "Failed to fetch orientation information from the content store. Trying from Exif.");
                            InputStream exifInputStream = null; // Note: Re-open to avoid issues with #reset()
                            try {
                                exifInputStream = mContentResolver.openInputStream(uri);
                                if (exifInputStream != null) {
                                    final ExifInterface exif = new ExifInterface(exifInputStream);
                                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                                    Logger.info(ImageImportProcessor.this, "Read exif orientation as {}", orientation);
                                }
                            } catch (IOException e) {
                                Logger.error(ImageImportProcessor.this, "An Exif parsing exception occurred", e);
                            } finally {
                                StorageManager.closeQuietly(exifInputStream);
                            }
                        }

                        if (orientation != ExifInterface.ORIENTATION_UNDEFINED) {
                            Logger.info(ImageImportProcessor.this, "Image orientation determined as {}. Rotating...", orientation);
                            bitmap = ImageUtils.rotateBitmap(bitmap, orientation);
                        } else {
                            Logger.warn(ImageImportProcessor.this, "Indeterminate orientation. Skipping rotation");
                        }
                    } else {
                        Logger.info(ImageImportProcessor.this, "Image import rotation is disabled. Ignoring...");
                    }

                    final File destination = mStorageManner.getFile(mTrip.getDirectory(), System.currentTimeMillis() + ".jpg");
                    if (!mStorageManner.writeBitmap(Uri.fromFile(destination), bitmap, Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY)) {
                        Logger.error(ImageImportProcessor.this, "Failed to write the image data. Aborting");
                        emitter.onError(new IOException("Failed to write the image data. Aborting"));
                    } else {
                        Logger.info(ImageImportProcessor.this, "Successfully saved the image to {}.", destination);
                        emitter.onSuccess(destination);
                    }
                } else {
                    emitter.onError(new FileNotFoundException());
                }
            } catch (IOException e) {
                emitter.onError(e);
            } finally {
                StorageManager.closeQuietly(inputStream);
            }
        });
    }

    private int getImageScaleFactor(@NonNull Uri uri) {
        InputStream inputStream = null;
        try {
            inputStream = mContentResolver.openInputStream(uri);
            if (inputStream != null) {

                final BitmapFactory.Options inJustDecodeBoundsOptions = new BitmapFactory.Options();
                inJustDecodeBoundsOptions.inJustDecodeBounds = true;

                // Decode data to our option bounds but don't read the full image
                BitmapFactory.decodeStream(inputStream, null, inJustDecodeBoundsOptions);

                int fullWidth = inJustDecodeBoundsOptions.outWidth;
                int fullHeight = inJustDecodeBoundsOptions.outHeight;
                int scale = 1;
                while (fullWidth > MAX_DIMENSION && fullHeight > MAX_DIMENSION) {
                    fullWidth >>>= 1;
                    fullHeight >>>= 1;
                    scale <<= 1;
                }
                return scale;
            }
        } catch (IOException e) {
            Logger.warn(this, "Failed to process image scale", e);
        } finally {
            StorageManager.closeQuietly(inputStream);
        }
        return 1;
    }

    private int getOrientationFromMediaStore(@NonNull Uri externalUri) {
        final boolean hasStoragePermission = ContextCompat.checkSelfPermission(mContext, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!hasStoragePermission) {
            Logger.warn(ImageImportProcessor.this, "The user has not provided storage permissions. Unable to determine the rotation from the content provider.");
            return ExifInterface.ORIENTATION_UNDEFINED;
        }

        Cursor cursor = null;
        try {
            final String[] imageColumns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION };
            cursor = mContentResolver.query(externalUri, imageColumns, null, null, null);
            if(cursor != null && cursor.moveToFirst() && cursor.getColumnCount() > 0) {
                return cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
            }
            else {
                Logger.warn(this, "Failed to find the URI to determine the orientation");
                return ExifInterface.ORIENTATION_UNDEFINED;
            }
        }
        catch (Exception e) {
            Logger.error(this, "An exception occurred when fetching the content orientation", e);
            return ExifInterface.ORIENTATION_UNDEFINED;
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }



}

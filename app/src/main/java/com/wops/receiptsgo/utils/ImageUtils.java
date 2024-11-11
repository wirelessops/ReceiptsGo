package com.wops.receiptsgo.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import co.smartreceipts.analytics.log.Logger;
import wb.android.storage.StorageManager;

public class ImageUtils {

    private ImageUtils() {
    }

    /**
     * Converts an image to grayscale
     *
     * @param bitmap - the {@link Bitmap} to convert to grayscale. The initial copy of which
     *               will be recycled via the {@link Bitmap#recycle()} method
     * @return - a grayscale {@link Bitmap} instance with {@link Bitmap.Config#RGB_565}. The initial
     * bitmap instance will be returned if an {@link java.lang.OutOfMemoryError} occurs
     */
    public static Bitmap convertToGrayScale(Bitmap bitmap) {
        return convertToGrayScale(bitmap, Bitmap.Config.RGB_565);
    }

    /**
     * Converts an image to grayscale
     *
     * @param bitmap - the {@link Bitmap} to convert to grayscale. The initial copy of which
     *               will be recycled via the {@link Bitmap#recycle()} method
     * @param config - the {@link Bitmap.Config} to create the new image with
     * @return - a grayscale {@link Bitmap} instance. The initial
     * bitmap instance will be returned if an {@link java.lang.OutOfMemoryError} occurs
     */
    public static Bitmap convertToGrayScale(Bitmap bitmap, Bitmap.Config config) {
        if (bitmap == null || config == null) {
            return bitmap;
        }
        try {
            Bitmap bmpGrayscale = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
            Canvas c = new Canvas(bmpGrayscale);
            Paint paint = new Paint();
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
            paint.setColorFilter(f);
            c.drawBitmap(bitmap, 0, 0, paint);
            if (bitmap != bmpGrayscale) {
                bitmap.recycle();
            }
            return bmpGrayscale;
        } catch (OutOfMemoryError e) {
            return bitmap;
        }
    }


    /**
     * Rotates the {@link Bitmap} to a given orientation
     *
     * @param bitmap      - bitmap to rotate
     * @param exifOrientation - the orientation to move to. This should not be a direct
     *                    value but rather one that is gathered via the {@link android.media.ExifInterface#getAttributeInt(String, int)}
     *                    value with the key {@link android.media.ExifInterface#TAG_ORIENTATION}.
     * @return - the rotated version of the original {@link Bitmap}. Please note that the initial one will
     * be recycled via the {@link Bitmap#recycle()} method if a rotation occurs. The original bitmap will be returned if an {@link java.lang.OutOfMemoryError} occurs
     * or it is already in the proper rotation
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int exifOrientation) {
        Matrix matrix = new Matrix();
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            Logger.error(ImageUtils.class, "Failed to rotate bitmap.", e);
            return bitmap;
        }
    }

    /**
     * When creating a JPG image from a PNG image, Android automatically makes the entire background
     * black if no background was defined (ie just alpha). This method allows us to force any bitmap
     * to have a white background, so this oddity will not appear when converting to a format without
     * alpha support (eg from PNG -> JPG).
     *
     * @param source the source {@link Bitmap}, which may have an alpha/transparent background. This
     *               bitmap will be recycled at the conclusion of this operation
     * @return the resultant {@link Bitmap}, which will have a white background
     */
    @NonNull
    public static Bitmap applyWhiteBackground(@NonNull Bitmap source) {
        try {
            final Bitmap whiteBackgroundBitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
            final Canvas canvas = new Canvas(whiteBackgroundBitmap);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(source, 0, 0, null);
            return whiteBackgroundBitmap;
        } finally {
            source.recycle();
        }
    }

    /**
     * Write a compressed version of the bitmap to the specified codec (eg from PNG to JPG). If
     * successful, the source bitmap will be recycled and the resultant one will be returned in the
     * desired format.
     *
     * <p>
     * Note: not all Formats support all bitmap configs directly, so it is possible that the
     * returned bitmap from BitmapFactory could be in a different bitdepth, and/or may have lost
     * per-pixel alpha (e.g. JPEG only supports opaque pixels).
     * </p>
     *
     * @param source the source {@link Bitmap} to use. It will be recycled automatically if the
     *               conversion succeeds
     * @param format the format of the compressed image
     * @param quality hint to the compressor, 0-100. 0 meaning compress for small size, 100 meaning
     *                compress for max quality. Some formats, like PNG which is lossless, will
     *                ignore the quality setting
     * @throws IOException if the conversion fails
     * @return the new {@link Bitmap}
     */
    @NonNull
    public static Bitmap changeCodec(@NonNull Bitmap source, @NonNull Bitmap.CompressFormat format,
                                     int quality) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            if (source.compress(format, quality, outputStream)) {
                final byte[] byteArray = outputStream.toByteArray();
                try {
                    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                } finally {
                    source.recycle();
                }
            } else {
                throw new IOException("Failed to convert the bitmap codec");
            }
        } catch (OutOfMemoryError e) {
            throw new IOException("Insufficient application memory to perform this operation");
        } finally {
            StorageManager.closeQuietly(outputStream);
        }
    }

    /**
     * Write a compressed version of the bitmap to the specified codec (eg from PNG to JPG). If
     * successful, the source bitmap will be recycled and the resultant one will be returned in the
     * desired format as an {@link OutputStream}
     *
     * <p>
     * Note: not all Formats support all bitmap configs directly, so it is possible that the
     * returned bitmap from BitmapFactory could be in a different bitdepth, and/or may have lost
     * per-pixel alpha (e.g. JPEG only supports opaque pixels).
     * </p>
     *
     * @param source the source {@link Bitmap} to use. It will be recycled automatically if the
     *               conversion succeeds
     * @param format the format of the compressed image
     * @param quality hint to the compressor, 0-100. 0 meaning compress for small size, 100 meaning
     *                compress for max quality. Some formats, like PNG which is lossless, will
     *                ignore the quality setting
     * @throws IOException if the conversion fails
     * @return an {@link IOException}, containing the bitmap with the converted codec
     */
    @NonNull
    public static OutputStream changeCodecToStream(@NonNull Bitmap source, @NonNull Bitmap.CompressFormat format,
                                           int quality) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (source.compress(format, quality, outputStream)) {
            return outputStream;
        } else {
            throw new IOException("Failed to convert the bitmap codec");
        }
    }

    /**
     * Attempts to draw a given {@link View} to a {@link Bitmap}
     *
     * @param view the desired {@link View} to draw
     * @return the {@link Bitmap}
     */
    @NonNull
    public static Bitmap drawView(@NonNull View view) {
        return drawView(view, 1);
    }

    /**
     * Attempts to draw a given {@link View} to a {@link Bitmap}
     *
     * @param view the desired {@link View} to draw
     * @param scaleFactor how much the image should be scaled up/down
     * @return the {@link Bitmap}
     */
    @NonNull
    public static Bitmap drawView(@NonNull View view, float scaleFactor) {
        final Bitmap bitmap = Bitmap.createBitmap((int) (view.getWidth() * scaleFactor), (int) (view.getHeight() * scaleFactor), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static boolean writeImageToFile(@NonNull Bitmap bitmap, @NonNull File file) {
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Nullable
    public static Bitmap getImageFromFile(@NonNull File file) {
        try {
            return BitmapFactory.decodeFile(file.getCanonicalPath());
        } catch (IOException e) {
            return null;
        }
    }
}

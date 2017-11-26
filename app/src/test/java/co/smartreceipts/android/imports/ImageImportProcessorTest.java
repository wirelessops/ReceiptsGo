package co.smartreceipts.android.imports;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowBitmap;
import org.robolectric.shadows.ShadowMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import co.smartreceipts.android.TestResourceReader;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import wb.android.storage.StorageManager;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ImageImportProcessorTest {

    /**
     * A simple test image that is 550x400
     */
    private static final String SAMPLE_JPG = "sample.jpg";

    /**
     * A simple test image that is 2200x1600, which we scale down for storage reasons
     */
    private static final String SAMPLE_JPG_BIG = "sample_big.jpg";

    /**
     * A simple test image that is 400x550, but it contains exif information to rotate back upright
     */
    private static final String SAMPLE_JPG_WITH_EXIF = "sample_with_exif_to_rotate.jpg";

    // Class under test
    ImageImportProcessor importProcessor;

    Context context;

    File destination;

    @Mock
    Trip trip;

    @Mock
    StorageManager storageManager;

    @Mock
    UserPreferenceManager preferences;

    @Mock
    ContentResolver contentResolver;

    @Captor
    ArgumentCaptor<Bitmap> bitmapCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        context = RuntimeEnvironment.application;
        destination = new File(context.getCacheDir(), "test.jpg");

        when(trip.getDirectory()).thenReturn(context.getCacheDir());
        when(storageManager.getFile(any(File.class), anyString())).thenReturn(destination);
        when(preferences.get(UserPreference.Camera.SaveImagesInGrayScale)).thenReturn(false);

        importProcessor = new ImageImportProcessor(trip, storageManager, preferences, context, contentResolver);
    }

    @Test
    public void importUriWithNullStream() throws Exception {
        final Uri uri = mock(Uri.class);
        when(contentResolver.openInputStream(uri)).thenReturn(null);

        importProcessor.process(uri)
                .test()
                .assertError(FileNotFoundException.class);
    }

    @Test
    public void importUriThrowsFileNotFoundException() throws Exception {
        final Uri uri = mock(Uri.class);
        when(contentResolver.openInputStream(uri)).thenThrow(new FileNotFoundException("test"));

        importProcessor.process(uri)
                .test()
                .assertError(FileNotFoundException.class);
    }

    @Test
    public void importUriWhenSaveFails() throws Exception {
        final Uri uri = Uri.fromFile(destination);
        configureUriForStream(uri, SAMPLE_JPG);
        when(preferences.get(UserPreference.Camera.AutomaticallyRotateImages)).thenReturn(false);
        when(storageManager.writeBitmap(any(Uri.class), bitmapCaptor.capture(), eq(Bitmap.CompressFormat.JPEG), eq(ImageImportProcessor.COMPRESSION_QUALITY))).thenReturn(false);

        importProcessor.process(uri)
                .test()
                .assertError(IOException.class);
    }

    @Test
    public void importUriWithoutAlterations() throws Exception {
        final Uri uri = Uri.fromFile(destination);
        configureUriForStream(uri, SAMPLE_JPG);
        when(preferences.get(UserPreference.Camera.AutomaticallyRotateImages)).thenReturn(false);
        when(storageManager.writeBitmap(any(Uri.class), bitmapCaptor.capture(), eq(Bitmap.CompressFormat.JPEG), eq(ImageImportProcessor.COMPRESSION_QUALITY))).thenReturn(true);

        importProcessor.process(uri)
                .test()
                .assertValue(destination)
                .assertComplete();

        final Bitmap bitmap = bitmapCaptor.getValue();
        assertNotNull(bitmap);
        assertEquals(550, bitmap.getWidth());
        assertEquals(400, bitmap.getHeight());
    }

    @Test
    public void importExifUriWithoutAlterations() throws Exception {
        final Uri uri = Uri.fromFile(destination);
        configureUriForStream(uri, SAMPLE_JPG_WITH_EXIF);
        when(preferences.get(UserPreference.Camera.AutomaticallyRotateImages)).thenReturn(false);
        when(storageManager.writeBitmap(any(Uri.class), bitmapCaptor.capture(), eq(Bitmap.CompressFormat.JPEG), eq(ImageImportProcessor.COMPRESSION_QUALITY))).thenReturn(true);

        importProcessor.process(uri)
                .test()
                .assertValue(destination)
                .assertComplete();

        final Bitmap bitmap = bitmapCaptor.getValue();
        assertNotNull(bitmap);

        // Confirm that it's sideways
        assertEquals(400, bitmap.getWidth());
        assertEquals(550, bitmap.getHeight());
    }

    @Test
    public void importUriScalesDownSizes() throws Exception {
        final Uri uri = Uri.fromFile(destination);
        configureUriForStream(uri, SAMPLE_JPG_BIG);
        when(preferences.get(UserPreference.Camera.AutomaticallyRotateImages)).thenReturn(false);
        when(storageManager.writeBitmap(any(Uri.class), bitmapCaptor.capture(), eq(Bitmap.CompressFormat.JPEG), eq(ImageImportProcessor.COMPRESSION_QUALITY))).thenReturn(true);

        importProcessor.process(uri)
                .test()
                .assertValue(destination)
                .assertComplete();

        // Note: we only scale down til one dimension is < 1024
        final Bitmap bitmap = bitmapCaptor.getValue();
        assertNotNull(bitmap);
        assertEquals(1100, bitmap.getWidth());
        assertEquals(800, bitmap.getHeight());
    }

    @Test
    public void importUriWithRotateOn() throws Exception {
        final Uri uri = Uri.fromFile(destination);
        configureUriForStream(uri, SAMPLE_JPG);
        when(preferences.get(UserPreference.Camera.AutomaticallyRotateImages)).thenReturn(true);
        when(storageManager.writeBitmap(any(Uri.class), bitmapCaptor.capture(), eq(Bitmap.CompressFormat.JPEG), eq(ImageImportProcessor.COMPRESSION_QUALITY))).thenReturn(true);

        importProcessor.process(uri)
                .test()
                .assertValue(destination)
                .assertComplete();

        final Bitmap bitmap = bitmapCaptor.getValue();
        assertNotNull(bitmap);
        assertEquals(550, bitmap.getWidth());
        assertEquals(400, bitmap.getHeight());
    }

    @Test
    public void importExifUriWithRotateOn() throws Exception {
        final Uri uri = Uri.fromFile(destination);
        configureUriForStream(uri, SAMPLE_JPG_WITH_EXIF);
        when(preferences.get(UserPreference.Camera.AutomaticallyRotateImages)).thenReturn(true);
        when(storageManager.writeBitmap(any(Uri.class), bitmapCaptor.capture(), eq(Bitmap.CompressFormat.JPEG), eq(ImageImportProcessor.COMPRESSION_QUALITY))).thenReturn(true);

        importProcessor.process(uri)
                .test()
                .assertValue(destination)
                .assertComplete();

        final Bitmap bitmap = bitmapCaptor.getValue();
        assertNotNull(bitmap);

        // TODO: Use direct getWidth/getHeight test once Robolectric 3.2 is available
        // TODO: Remove this shadow hack once the Robolectric supports this rotation via ShadowBitmap
        // TODO: assertEquals(550, bitmap.getWidth());
        // TODO: assertEquals(400, bitmap.getHeight());

        // Confirm that we have a matrix to rotate this
        final ShadowBitmap shadowBitmap = Shadows.shadowOf(bitmap);
        final ShadowMatrix shadowMatrix = Shadows.shadowOf(shadowBitmap.getCreatedFromMatrix());
        assertEquals(shadowMatrix.getSetOperations().get(ShadowMatrix.ROTATE), "90.0");
    }

    private void configureUriForStream(@NonNull Uri uri, @NonNull String imageFile) throws Exception {
        when(contentResolver.openInputStream(uri))
                .thenReturn(new TestResourceReader().openStream(imageFile),
                        new TestResourceReader().openStream(imageFile),
                        new TestResourceReader().openStream(imageFile));
    }

}
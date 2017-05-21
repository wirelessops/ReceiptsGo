package co.smartreceipts.android.imports;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.settings.UserPreferenceManager;
import wb.android.storage.StorageManager;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class FileImportProcessorFactoryTest {

    // Class under test
    FileImportProcessorFactory factory;

    @Mock
    Trip trip;

    @Mock
    StorageManager storageManager;

    @Mock
    UserPreferenceManager preferenceManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.factory = new FileImportProcessorFactory(RuntimeEnvironment.application, preferenceManager, storageManager);
    }

    @Test
    public void get() {
        // Image Imports
        assertTrue(this.factory.get(RequestCodes.NATIVE_ADD_PHOTO_CAMERA_REQUEST, trip) instanceof ImageImportProcessor);
        assertTrue(this.factory.get(RequestCodes.NATIVE_NEW_RECEIPT_CAMERA_REQUEST, trip) instanceof ImageImportProcessor);
        assertTrue(this.factory.get(RequestCodes.NATIVE_RETAKE_PHOTO_CAMERA_REQUEST, trip) instanceof ImageImportProcessor);
        assertTrue(this.factory.get(RequestCodes.IMPORT_GALLERY_IMAGE, trip) instanceof ImageImportProcessor);

        // PDF Imports
        assertTrue(this.factory.get(RequestCodes.IMPORT_GALLERY_PDF, trip) instanceof GenericFileImportProcessor);

        // Rest are auto fail
        assertTrue(this.factory.get(-1, trip) instanceof AutoFailImportProcessor);
        assertTrue(this.factory.get(0, trip) instanceof AutoFailImportProcessor);
        assertTrue(this.factory.get(Integer.MAX_VALUE, trip) instanceof AutoFailImportProcessor);
        assertTrue(this.factory.get(Integer.MIN_VALUE, trip) instanceof AutoFailImportProcessor);
    }

}
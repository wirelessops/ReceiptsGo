package com.wops.receiptsgo.imports;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.settings.UserPreferenceManager;
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
        this.factory = new FileImportProcessorFactory(ApplicationProvider.getApplicationContext(), preferenceManager, storageManager);
    }

    @Test
    public void get() {
        // Image Imports
        assertTrue(this.factory.get(RequestCodes.ATTACH_CAMERA_IMAGE, trip) instanceof ImageImportProcessor);
        assertTrue(this.factory.get(RequestCodes.NEW_RECEIPT_CAMERA_IMAGE, trip) instanceof ImageImportProcessor);
        assertTrue(this.factory.get(RequestCodes.RETAKE_CAMERA_IMAGE, trip) instanceof ImageImportProcessor);
        assertTrue(this.factory.get(RequestCodes.NEW_RECEIPT_IMPORT_IMAGE, trip) instanceof ImageImportProcessor);

        // PDF Imports
        assertTrue(this.factory.get(RequestCodes.NEW_RECEIPT_IMPORT_PDF, trip) instanceof GenericFileImportProcessor);

        // Rest are auto fail
        assertTrue(this.factory.get(-1, trip) instanceof AutoFailImportProcessor);
        assertTrue(this.factory.get(0, trip) instanceof AutoFailImportProcessor);
        assertTrue(this.factory.get(Integer.MAX_VALUE, trip) instanceof AutoFailImportProcessor);
        assertTrue(this.factory.get(Integer.MIN_VALUE, trip) instanceof AutoFailImportProcessor);
    }

}
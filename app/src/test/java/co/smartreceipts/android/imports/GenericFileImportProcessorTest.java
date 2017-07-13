package co.smartreceipts.android.imports;

import android.content.ContentResolver;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import co.smartreceipts.android.model.Trip;
import wb.android.storage.StorageManager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class GenericFileImportProcessorTest {

    GenericFileImportProcessor importProcessor;

    @Mock
    Trip trip;

    @Mock
    StorageManager storageManner;

    @Mock
    ContentResolver contentResolver;

    @Mock
    File file;

    @Mock
    InputStream inputStream;

    Uri uri;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        uri = Uri.parse("content://some.pdf");
        when(contentResolver.getType(uri)).thenReturn("application/pdf");
        when(storageManner.getFile(any(File.class), anyString())).thenReturn(file);

        importProcessor = new GenericFileImportProcessor(trip, storageManner, contentResolver);
    }

    @Test
    public void processThrowsFileNotFoundException() throws Exception {
        when(contentResolver.openInputStream(uri)).thenThrow(new FileNotFoundException("Test"));

        importProcessor.process(uri)
                .test()
                .assertNoValues()
                .assertNotComplete()
                .assertError(FileNotFoundException.class);
    }

    @Test
    public void processFailsToCopy() throws Exception {
        when(contentResolver.openInputStream(uri)).thenReturn(inputStream);
        when(storageManner.copy(inputStream, file, true)).thenReturn(false);

        importProcessor.process(uri)
                .test()
                .assertNoValues()
                .assertNotComplete()
                .assertError(FileNotFoundException.class);
    }

    @Test
    public void processSuccess() throws Exception {
        when(contentResolver.openInputStream(uri)).thenReturn(inputStream);
        when(storageManner.copy(inputStream, file, true)).thenReturn(true);

        importProcessor.process(uri)
                .test()
                .assertValue(file)
                .assertComplete()
                .assertNoErrors();
    }

}
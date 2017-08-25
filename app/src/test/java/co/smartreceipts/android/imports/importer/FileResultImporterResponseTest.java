package co.smartreceipts.android.imports.importer;

import org.junit.Test;
import org.mockito.Mock;

import java.io.File;

import co.smartreceipts.android.ocr.apis.model.OcrResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileResultImporterResponseTest {

    @Mock
    OcrResponse ocrResponse;

    @Test
    public void errorTest() {
        final ActivityFileResultImporterResponse response = ActivityFileResultImporterResponse.importerError(new Exception());

        assertTrue(response.getThrowable().isPresent());
        assertEquals(null, response.getFile());
        assertEquals(null, response.getOcrResponse());
        assertEquals(0, response.getRequestCode());
        assertEquals(0, response.getResultCode());
    }

    @Test
    public void responseTest() {
        File file = new File("");

        final ActivityFileResultImporterResponse response = ActivityFileResultImporterResponse.importerResponse(file, ocrResponse, 1, 1);

        assertFalse(response.getThrowable().isPresent());
        assertEquals(file, response.getFile());
        assertEquals(ocrResponse, response.getOcrResponse());
        assertEquals(1, response.getRequestCode());
        assertEquals(1, response.getResultCode());
    }
}

package co.smartreceipts.android.imports.importer

import co.smartreceipts.android.ocr.apis.model.OcrResponse
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.File

class FileResultImporterResponseTest {

    @Mock
    lateinit var ocrResponse: OcrResponse

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun errorTest() {
        val (throwable, file, ocrResponse1, requestCode, resultCode) =
            ActivityFileResultImporterResponse.importerError(Exception())

        assertTrue(throwable.isPresent)
        assertNull(file)
        assertNull(ocrResponse1)
        assertEquals(0, requestCode)
        assertEquals(0, resultCode)
    }

    @Test
    fun responseTest() {
        val file = File("")

        val response: ActivityFileResultImporterResponse =
            ActivityFileResultImporterResponse.importerResponse(file, ocrResponse, 1, 1)

        assertFalse(response.throwable.isPresent)
        assertEquals(file, response.file)
        assertEquals(ocrResponse, response.ocrResponse)
        assertEquals(1, response.requestCode)
        assertEquals(1, response.resultCode)
    }
}

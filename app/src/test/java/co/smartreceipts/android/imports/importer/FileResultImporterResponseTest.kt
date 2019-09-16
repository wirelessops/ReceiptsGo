package co.smartreceipts.android.imports.importer

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class FileResultImporterResponseTest {

    @Test
    fun errorTest() {
        val (throwable, file, requestCode, resultCode) =
            ActivityFileResultImporterResponse.importerError(Exception())

        assertTrue(throwable.isPresent)
        assertNull(file)
        assertEquals(0, requestCode)
        assertEquals(0, resultCode)
    }

    @Test
    fun responseTest() {
        val file = File("")

        val response: ActivityFileResultImporterResponse =
            ActivityFileResultImporterResponse.importerResponse(file, 1, 1)

        assertFalse(response.throwable.isPresent)
        assertEquals(file, response.file)
        assertEquals(1, response.requestCode)
        assertEquals(1, response.resultCode)
    }
}

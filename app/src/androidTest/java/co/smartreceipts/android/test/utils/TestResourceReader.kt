package co.smartreceipts.android.test.utils

import java.io.File
import java.io.InputStream

class TestResourceReader {

    fun openFile(resourceName: String): File {
        return File(javaClass.classLoader.getResource(resourceName).file)
    }

    fun openStream(resourceName: String): InputStream {
        return javaClass.classLoader.getResourceAsStream(resourceName)
    }

    companion object {
        // Note: Don't put these in sub-folders in the resources dir as everything blows up
        const val V15_DATABASE = "v15_receipts.db"
        const val V15_IMAGE = "v15_img.jpg"
        const val V15_PDF = "v15_pdf.pdf"
    }
}

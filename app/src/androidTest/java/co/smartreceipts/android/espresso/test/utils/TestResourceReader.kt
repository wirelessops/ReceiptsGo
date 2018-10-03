package co.smartreceipts.android.espresso.test.utils

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
        const val DATABASE_V15 = "receipts_v15.db"
    }
}

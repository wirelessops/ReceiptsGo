package com.wops.receiptsgo.imports.importer

import android.os.Parcelable
import com.hadisatrio.optional.Optional
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class ActivityFileResultImporterResponse(
    /**
     * Throwable if the importer produced an error
     */
    val throwable: Optional<Throwable>,

    /**
     * The resultant file that was imported
     */
    val file: File? = null,

    /**
     * The request code that triggered the import
     */
    val requestCode: Int = 0,

    /**
     * The result code that from the response
     */
    val resultCode: Int = 0
) : Parcelable {

    companion object {

        @JvmStatic
        fun importerError(throwable: Throwable): ActivityFileResultImporterResponse =
            ActivityFileResultImporterResponse(Optional.of(throwable))

        @JvmStatic
        fun importerResponse(file: File, requestCode: Int, resultCode: Int): ActivityFileResultImporterResponse =
            ActivityFileResultImporterResponse(Optional.absent(), file, requestCode, resultCode)

    }
}

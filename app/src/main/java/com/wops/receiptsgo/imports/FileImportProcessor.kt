package com.wops.receiptsgo.imports

import android.net.Uri
import io.reactivex.Single
import java.io.File

interface FileImportProcessor {

    /**
     * Processes a uri to get an Android file
     * @param uri the desired [Uri]
     * @return the resultant [File]
     */
    fun process(uri: Uri): Single<File>
}

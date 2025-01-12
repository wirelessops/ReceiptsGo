package com.wops.receiptsgo.imports

import android.net.Uri
import io.reactivex.Single
import java.io.File

class AutoFailImportProcessor : FileImportProcessor {

    override fun process(uri: Uri): Single<File> {
        return Single.error(UnsupportedOperationException())
    }
}

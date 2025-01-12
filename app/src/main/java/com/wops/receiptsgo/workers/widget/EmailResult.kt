package com.wops.receiptsgo.workers.widget

import android.content.Intent
import android.net.Uri

sealed class EmailResult {

    class Error(val errorType: GenerationErrors) : EmailResult()

    class Success(val intent: Intent? = null, val uris: List<Uri>? = null) : EmailResult()

    object InProgress: EmailResult()
}

enum class GenerationErrors {
    ERROR_UNDETERMINED,
    ERROR_NO_SELECTION,
    ERROR_NO_RECEIPTS,
    ERROR_DISABLED_DISTANCES,
    ERROR_TOO_MANY_COLUMNS,
    ERROR_PDF_GENERATION,
    ERROR_MEMORY,
    ERROR_FILE_COPY
}
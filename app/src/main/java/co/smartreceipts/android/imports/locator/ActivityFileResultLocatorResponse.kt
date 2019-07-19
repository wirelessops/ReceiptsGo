package co.smartreceipts.android.imports.locator

import android.net.Uri

import com.hadisatrio.optional.Optional

data class ActivityFileResultLocatorResponse private constructor(
    val throwable: Optional<Throwable>,
    val uri: Uri? = null,
    val requestCode: Int = 0,
    val resultCode: Int = 0
) {

    companion object {
        @JvmStatic
        fun locatorError(throwable: Throwable) = ActivityFileResultLocatorResponse(Optional.of(throwable))

        @JvmStatic
        fun locatorResponse(uri: Uri, requestCode: Int, resultCode: Int) =
            ActivityFileResultLocatorResponse(Optional.absent(), uri, requestCode, resultCode)
    }
}


package com.wops.receiptsgo.ocr.widget.tooltip

import android.content.SharedPreferences

import javax.inject.Inject

import co.smartreceipts.core.di.scopes.ApplicationScope
import dagger.Lazy
import io.reactivex.Single

@ApplicationScope
class OcrInformationalTooltipStateTracker @Inject constructor(private val preferences: Lazy<SharedPreferences>) {

    fun shouldShowOcrInfo(): Single<Boolean> {
        return Single.fromCallable { preferences.get().getBoolean(KEY_SHOW_OCR_RELEASE_INFO, true) }
    }

    fun setShouldShowOcrInfo(shouldShow: Boolean) {
        val editor = preferences.get().edit()
        editor.putBoolean(KEY_SHOW_OCR_RELEASE_INFO, shouldShow)
        editor.putLong(KEY_SHOW_OCR_RELEASE_SET_DATE, System.currentTimeMillis())
        editor.apply()
    }

    companion object {

        private const val KEY_SHOW_OCR_RELEASE_INFO = "key_show_ocr_release_info"
        private const val KEY_SHOW_OCR_RELEASE_SET_DATE = "key_show_ocr_release_info_set_date"
    }

}

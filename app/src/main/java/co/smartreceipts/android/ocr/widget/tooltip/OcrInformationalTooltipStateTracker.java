package co.smartreceipts.android.ocr.widget.tooltip;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.di.scopes.FragmentScope;
import io.reactivex.Single;


@FragmentScope
public class OcrInformationalTooltipStateTracker {

    private static final String KEY_SHOW_OCR_RELEASE_INFO = "key_show_ocr_release_info";
    private static final String KEY_SHOW_OCR_RELEASE_SET_DATE = "key_show_ocr_release_info_set_date";

    private final SharedPreferences preferences;

    public OcrInformationalTooltipStateTracker(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public OcrInformationalTooltipStateTracker(@NonNull SharedPreferences sharedPreferences) {
        preferences = Preconditions.checkNotNull(sharedPreferences);
    }

    public Single<Boolean> shouldShowOcrInfo() {
        return Single.fromCallable(() -> preferences.getBoolean(KEY_SHOW_OCR_RELEASE_INFO, true));
    }

    public void setShouldShowOcrInfo(boolean shouldShow) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_SHOW_OCR_RELEASE_INFO, shouldShow);
        editor.putLong(KEY_SHOW_OCR_RELEASE_SET_DATE, System.currentTimeMillis());
        editor.apply();
    }

}

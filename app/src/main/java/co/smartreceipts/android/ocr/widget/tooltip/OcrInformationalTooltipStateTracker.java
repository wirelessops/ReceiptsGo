package co.smartreceipts.android.ocr.widget.tooltip;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import dagger.Lazy;
import io.reactivex.Single;

@ApplicationScope
public class OcrInformationalTooltipStateTracker {

    private static final String KEY_SHOW_OCR_RELEASE_INFO = "key_show_ocr_release_info";
    private static final String KEY_SHOW_OCR_RELEASE_SET_DATE = "key_show_ocr_release_info_set_date";

    private final Lazy<SharedPreferences> preferences;

    @Inject
    public OcrInformationalTooltipStateTracker(@NonNull Lazy<SharedPreferences> sharedPreferences) {
        preferences = Preconditions.checkNotNull(sharedPreferences);
    }

    public Single<Boolean> shouldShowOcrInfo() {
        return Single.fromCallable(() -> preferences.get().getBoolean(KEY_SHOW_OCR_RELEASE_INFO, true));
    }

    public void setShouldShowOcrInfo(boolean shouldShow) {
        final SharedPreferences.Editor editor = preferences.get().edit();
        editor.putBoolean(KEY_SHOW_OCR_RELEASE_INFO, shouldShow);
        editor.putLong(KEY_SHOW_OCR_RELEASE_SET_DATE, System.currentTimeMillis());
        editor.apply();
    }

}

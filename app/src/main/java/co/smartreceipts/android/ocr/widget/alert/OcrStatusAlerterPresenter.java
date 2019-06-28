package co.smartreceipts.android.ocr.widget.alert;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.ocr.OcrManager;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.model.UiIndicator;
import co.smartreceipts.android.widget.mvp.BasePresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;

@FragmentScope
public class OcrStatusAlerterPresenter extends BasePresenter<OcrStatusAlerterView> {

    private final Context context;
    private final OcrManager ocrManager;

    @Inject
    public OcrStatusAlerterPresenter(@NonNull OcrStatusAlerterView view, @NonNull Context context, @NonNull OcrManager ocrManager) {
        super(view);
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.ocrManager = Preconditions.checkNotNull(ocrManager);
    }

    @Override
    public void subscribe() {
        compositeDisposable.add(ocrManager.getOcrProcessingStatus()
                .doOnNext(ocrProcessingStatus -> Logger.debug(OcrStatusAlerterPresenter.this, "Displaying OCR Status: {}", ocrProcessingStatus))
                .map(ocrProcessingStatus -> {
                    if (ocrProcessingStatus == OcrProcessingStatus.UploadingImage) {
                        return UiIndicator.loading(context.getString(R.string.ocr_status_message_uploading_image));
                    } else if (ocrProcessingStatus == OcrProcessingStatus.PerformingScan) {
                        return UiIndicator.loading(context.getString(R.string.ocr_status_message_performing_scan));
                    } else if (ocrProcessingStatus == OcrProcessingStatus.RetrievingResults) {
                        return UiIndicator.loading(context.getString(R.string.ocr_status_message_fetching_results));
                    } else {
                        return UiIndicator.<String>idle();
                    }
                })
                .startWith(UiIndicator.idle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::displayOcrStatus));
    }

    @Override
    public void unsubscribe() {
        view.displayOcrStatus(UiIndicator.idle()); // Hide our alert on disposal
        super.unsubscribe();
    }
}

package com.wops.receiptsgo.ocr.widget.alert;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import com.wops.receiptsgo.R;
import co.smartreceipts.core.di.scopes.FragmentScope;
import com.wops.receiptsgo.ocr.OcrManager;
import co.smartreceipts.analytics.log.Logger;
import com.wops.receiptsgo.widget.model.UiIndicator;
import com.wops.receiptsgo.widget.mvp.BasePresenter;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

@FragmentScope
public class OcrStatusAlerterPresenter extends BasePresenter<OcrStatusAlerterView> {

    private final Context context;
    private final OcrManager ocrManager;
    private final Scheduler observeOnScheduler;

    @Inject
    public OcrStatusAlerterPresenter(@NonNull OcrStatusAlerterView view, @NonNull Context context, @NonNull OcrManager ocrManager) {
        this(view, context, ocrManager, AndroidSchedulers.mainThread());
    }

    @VisibleForTesting
    OcrStatusAlerterPresenter(@NonNull OcrStatusAlerterView view, @NonNull Context context, @NonNull OcrManager ocrManager, @NonNull Scheduler observeOnScheduler) {
        super(view);
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.ocrManager = Preconditions.checkNotNull(ocrManager);
        this.observeOnScheduler = observeOnScheduler;
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
                .observeOn(observeOnScheduler)
                .subscribe(view::displayOcrStatus));
    }

    @Override
    public void unsubscribe() {
        view.displayOcrStatus(UiIndicator.idle()); // Hide our alert on disposal
        super.unsubscribe();
    }
}

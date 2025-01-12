package com.wops.receiptsgo.ocr.widget.alert;

import androidx.annotation.NonNull;

import com.wops.receiptsgo.widget.model.UiIndicator;

/**
 * A simple view contract for displaying the active status of the OCR process
 */
public interface OcrStatusAlerterView {

    /**
     * Indicates that we should display an alert to the user about the current state of an OCR (which
     * may be idle to indicate that nothing is happening)
     *
     * @param ocrStatusIndicator the current OCR Status {@link UiIndicator}
     */
    void displayOcrStatus(@NonNull UiIndicator<String> ocrStatusIndicator);

}

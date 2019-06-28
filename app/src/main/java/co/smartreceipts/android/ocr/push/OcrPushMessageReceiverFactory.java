package co.smartreceipts.android.ocr.push;

import androidx.annotation.NonNull;

public class OcrPushMessageReceiverFactory  {

    @NonNull
    public OcrPushMessageReceiver get() {
        return new OcrPushMessageReceiver();
    }

}

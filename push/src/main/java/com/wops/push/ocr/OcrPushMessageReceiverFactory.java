package com.wops.push.ocr;

import androidx.annotation.NonNull;

public class OcrPushMessageReceiverFactory  {

    @NonNull
    public OcrPushMessageReceiver get() {
        return new OcrPushMessageReceiver();
    }

}

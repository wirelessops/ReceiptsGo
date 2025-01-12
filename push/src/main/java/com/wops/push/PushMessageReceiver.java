package com.wops.push;

import androidx.annotation.NonNull;

import io.reactivex.Observable;

public interface PushMessageReceiver {

    /**
     * Called whenever a remote push message is received from Firebase Cloud Messaging
     *
     * @param remoteMessage the to handle
     */
    void onMessageReceived(@NonNull Object remoteMessage);

    Observable<Object> getPushResponse();
}

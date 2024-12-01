package com.wops.push.internal;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import io.reactivex.Observable;


public class FcmTokenRetriever {

    @NonNull
    private Task<String> getToken() {
        return FirebaseMessaging.getInstance().getToken();
    }

    @NonNull
    public Observable<String> getFcmTokenObservable() {
        return Observable.create(emitter -> {
            getToken().addOnSuccessListener(instanceIdResult -> {
                emitter.onNext(instanceIdResult);
                emitter.onComplete();
            });
        });
    }
}

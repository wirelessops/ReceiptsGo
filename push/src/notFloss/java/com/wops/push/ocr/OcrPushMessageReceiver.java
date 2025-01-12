package com.wops.push.ocr;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.TimeUnit;

import com.wops.push.PushMessageReceiver;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;


public class OcrPushMessageReceiver implements PushMessageReceiver {

    private static final int TIMEOUT_SECONDS = 15;

    private final Subject<Object> pushResultSubject = PublishSubject.create();
    private final Scheduler subscribeOnScheduler;

    OcrPushMessageReceiver() {
        this(Schedulers.io());
    }

    OcrPushMessageReceiver(@NonNull Scheduler subscribeOnScheduler) {
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    @Override
    public void onMessageReceived(@NonNull Object remoteMessage) {
        Preconditions.checkArgument(remoteMessage instanceof RemoteMessage);
        Observable.just(remoteMessage)
                .subscribeOn(subscribeOnScheduler)
                .map(message -> new Object())
                .subscribe(next -> {
                        pushResultSubject.onNext(next);
                        pushResultSubject.onComplete();
                });
    }

    @Override
    public Observable<Object> getPushResponse() {
        return pushResultSubject
                .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }


}

package com.wops.receiptsgo.widget;

import android.os.Looper;

import io.reactivex.Observer;

public class ThreadPreconditions {

    public static boolean checkMainThread(Observer<?> observer) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            observer.onError(new IllegalStateException(
                    "Expected to be called on the main thread but was " + Thread.currentThread().getName()));
            return false;
        }
        return true;
    }

    private ThreadPreconditions() {
        throw new AssertionError("No instances.");
    }
}

package co.smartreceipts.android.push.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.iid.FirebaseInstanceId;

import io.reactivex.Observable;


public class FcmTokenRetriever {

    @Nullable
    public String getToken() {
        return FirebaseInstanceId.getInstance().getToken();
    }

    @NonNull
    public Observable<String> getFcmTokenObservable() {
        return Observable.create(emitter -> {
            final String token = getToken();
            if (token != null) {
                emitter.onNext(token);
            }
            emitter.onComplete();
        });
    }
}

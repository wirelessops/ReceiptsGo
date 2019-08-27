package co.smartreceipts.android.push.internal;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import io.reactivex.Observable;


public class FcmTokenRetriever {

    @NonNull
    public Task<InstanceIdResult> getToken() {
        return FirebaseInstanceId.getInstance().getInstanceId();
    }

    @NonNull
    public Observable<String> getFcmTokenObservable() {
        return Observable.create(emitter -> {
            getToken().addOnSuccessListener(instanceIdResult -> {
                emitter.onNext(instanceIdResult.getToken());
                emitter.onComplete();
            });
        });
    }
}

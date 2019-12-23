package co.smartreceipts.aws.s3;

import androidx.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.subjects.ReplaySubject;


class S3KeyGeneratorFactory {

    private ReplaySubject<S3KeyGenerator> s3KeyGeneratorReplaySubject;

    @NonNull
    public synchronized Observable<S3KeyGenerator> get() {
        if (s3KeyGeneratorReplaySubject == null) {
            s3KeyGeneratorReplaySubject = ReplaySubject.create(1);
            Observable.<S3KeyGenerator>create(emitter -> {
                emitter.onNext(new S3KeyGenerator());
                emitter.onComplete();
            })
                    .subscribe(s3KeyGeneratorReplaySubject);
        }
        return s3KeyGeneratorReplaySubject;
    }
}

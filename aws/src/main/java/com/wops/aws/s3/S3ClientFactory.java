package com.wops.aws.s3;

import androidx.annotation.NonNull;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import com.wops.aws.cognito.CognitoManager;
import com.wops.aws.cognito.CognitoManagerImpl;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.ReplaySubject;


class S3ClientFactory {

    private final CognitoManager cognitoManager;

    private ReplaySubject<Optional<AmazonS3Client>> amazonS3ReplaySubject;
    private Disposable amazonS3Disposable;

    public S3ClientFactory(@NonNull CognitoManager cognitoManager) {
        this.cognitoManager = Preconditions.checkNotNull(cognitoManager);
    }

    /**
     * @return an {@link Optional} instance of the {@link AmazonS3} client. Once we fetch a valid
     * entry, this should be treated as a singleton for the lifetime of the parent
     * {@link S3ClientFactory} object, since we use a replay subject
     */
    @NonNull
    synchronized Observable<Optional<AmazonS3Client>> getAmazonS3() {
        if (amazonS3ReplaySubject == null) {
            amazonS3ReplaySubject = ReplaySubject.createWithSize(1);

            if (cognitoManager instanceof CognitoManagerImpl) {
                amazonS3Disposable = ((CognitoManagerImpl)cognitoManager).getCognitoCachingCredentialsProvider()
                        .<Optional<AmazonS3Client>>map(cognitoCachingCredentialsProvider -> {
                            if (cognitoCachingCredentialsProvider.isPresent()) {
                                return Optional.of(new AmazonS3Client(
                                        cognitoCachingCredentialsProvider.get(), Region.getRegion(Regions.US_EAST_1)));
                            } else {
                                return Optional.absent();
                            }
                        })
                        .subscribe(amazonS3ClientOptional -> {
                                    amazonS3ReplaySubject.onNext(amazonS3ClientOptional);
                                    if (amazonS3ClientOptional.isPresent()) {
                                        amazonS3ReplaySubject.onComplete();
                                        if (amazonS3Disposable != null) {
                                            amazonS3Disposable.dispose();
                                        }
                                    }
                                }, throwable -> amazonS3ReplaySubject.onError(throwable),
                                () -> amazonS3ReplaySubject.onComplete());

            }
        }
        return amazonS3ReplaySubject;
    }


}

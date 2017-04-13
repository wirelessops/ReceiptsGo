package co.smartreceipts.android.aws.s3;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.io.File;

import javax.inject.Inject;

import co.smartreceipts.android.aws.cognito.CognitoManager;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

@ApplicationScope
public class S3Manager {

    private static final String BUCKET = "smartreceipts";

    private final S3ClientFactory s3ClientFactory;
    private final S3KeyGeneratorFactory s3KeyGeneratorFactory;

    @Inject
    public S3Manager(Context context, CognitoManager cognitoManager) {
        this(new S3ClientFactory(context, cognitoManager), new S3KeyGeneratorFactory());
    }

    @VisibleForTesting
    S3Manager(@NonNull S3ClientFactory s3ClientFactory, @NonNull S3KeyGeneratorFactory s3KeyGeneratorFactory) {
        this.s3ClientFactory = Preconditions.checkNotNull(s3ClientFactory);
        this.s3KeyGeneratorFactory = Preconditions.checkNotNull(s3KeyGeneratorFactory);
    }

    /**
     * Uploads a file to this user's S3 cognito account
     *
     * @param file the {@link File} to upload
     * @param subDirectoryPath the subdirectory path to load to (either an empty string for the bucket or something more complex as desired)
     * @return an {@link Observable} that will return the URL path as a string if valid
     */
    @NonNull
    public Observable<String> upload(@NonNull final File file, @NonNull final String subDirectoryPath) {
        return s3ClientFactory.getAmazonS3()
                .flatMap(new Func1<Optional<AmazonS3Client>, Observable<String>>() {
                    @Override
                    public Observable<String> call(@NonNull final Optional<AmazonS3Client> amazonS3) {
                        if (amazonS3.isPresent()) {
                            return s3KeyGeneratorFactory.get()
                                    .flatMap(new Func1<S3KeyGenerator, Observable<String>>() {
                                        @Override
                                        public Observable<String> call(@NonNull final S3KeyGenerator s3KeyGenerator) {
                                            return Observable.create(new Observable.OnSubscribe<String>() {
                                                @Override
                                                public void call(final Subscriber<? super String> subscriber) {
                                                    try {
                                                        final String key = subDirectoryPath + s3KeyGenerator.getS3Key() + file.getName();
                                                        amazonS3.get().putObject(BUCKET, key, file);
                                                        subscriber.onNext(amazonS3.get().getResourceUrl(BUCKET, key));
                                                        subscriber.onCompleted();
                                                    } catch (Exception e) {
                                                        Logger.error(S3Manager.this, "Failed to upload to S3 with error.", e);
                                                        subscriber.onError(e);
                                                    }
                                                }
                                            });
                                        }
                                    });
                        } else {
                            return Observable.error(new Exception("Failed to initialize the S3 client"));
                        }
                    }
                });
    }

}

package com.wops.aws.s3;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import com.wops.aws.cognito.CognitoManager;
import com.wops.core.di.scopes.ApplicationScope;
import io.reactivex.Observable;


@ApplicationScope
public class S3Manager {

    private static final String BUCKET = "smartreceipts";

    private final S3ClientFactory s3ClientFactory;
    private final S3KeyGeneratorFactory s3KeyGeneratorFactory;

    @Inject
    public S3Manager(CognitoManager cognitoManager) {
        this(new S3ClientFactory(cognitoManager), new S3KeyGeneratorFactory());
    }

    @VisibleForTesting
    S3Manager(@NonNull S3ClientFactory s3ClientFactory, @NonNull S3KeyGeneratorFactory s3KeyGeneratorFactory) {
        this.s3ClientFactory = Preconditions.checkNotNull(s3ClientFactory);
        this.s3KeyGeneratorFactory = Preconditions.checkNotNull(s3KeyGeneratorFactory);
    }

    /**
     * Uploads a file to this user's S3 cognito account
     *
     * @param file             the {@link File} to upload
     * @param subDirectoryPath the subdirectory path to load to (either an empty string for the bucket or something more complex as desired)
     * @return an {@link Observable} that will return the URL path as a string if valid
     */
    @NonNull
    public Observable<String> upload(@NonNull final File file, @NonNull final String subDirectoryPath) {
        return s3ClientFactory.getAmazonS3()
                .flatMap(amazonS3 -> {
                    if (amazonS3.isPresent()) {
                        return s3KeyGeneratorFactory.get()
                                .flatMap(s3KeyGenerator -> Observable.fromCallable(() -> {
                                    final String key = subDirectoryPath + s3KeyGenerator.getS3Key() + file.getName();
                                    amazonS3.get().putObject(BUCKET, key, file);
                                    try {
                                        return amazonS3.get().getResourceUrl(BUCKET, key);
                                    } catch (Exception e) {
                                        throw new IOException("Caught Amazon S3 exception", e);
                                    }
                                }));
                    } else {
                        return Observable.error(new Exception("Failed to initialize the S3 client"));
                    }
                });
    }
}

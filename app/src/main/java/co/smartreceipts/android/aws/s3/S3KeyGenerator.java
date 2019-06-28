package co.smartreceipts.android.aws.s3;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Simple class for generating a secure/unique name for the file name that we're using
 */
class S3KeyGenerator {

    private final SecureRandom random = new SecureRandom();

    @NonNull
    public String getS3Key() {
        return new BigInteger(130, random).toString(32);
    }
}

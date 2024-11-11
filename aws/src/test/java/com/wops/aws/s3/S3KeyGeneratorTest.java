package com.wops.aws.s3;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class S3KeyGeneratorTest {

    @Test
    public void getS3Key() {
        final S3KeyGenerator generator = new S3KeyGenerator();
        final String key = generator.getS3Key();
        assertNotNull(key);
        assertTrue(key.length() > 16);
    }

}
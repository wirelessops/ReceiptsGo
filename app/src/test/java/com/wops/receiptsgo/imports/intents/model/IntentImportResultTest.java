package com.wops.receiptsgo.imports.intents.model;

import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class IntentImportResultTest {

    @Test
    public void getters() {
        final IntentImportResult result = new IntentImportResult(Uri.EMPTY, FileType.Image);
        assertEquals(Uri.EMPTY, result.getUri());
        assertEquals(FileType.Image, result.getFileType());
    }

}
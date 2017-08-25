package co.smartreceipts.android.imports.locator;

import android.net.Uri;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileResultLocatorResponseTest {

    @Test
    public void errorTest() {
        final ActivityFileResultLocatorResponse response = ActivityFileResultLocatorResponse.LocatorError(new Exception());

        assertTrue(response.getThrowable().isPresent());
        assertEquals(null, response.getUri());
        assertEquals(0, response.getRequestCode());
        assertEquals(0, response.getResultCode());
    }

    @Test
    public void responseTest() {
        final Uri uri = Uri.EMPTY;
        final ActivityFileResultLocatorResponse response = ActivityFileResultLocatorResponse.LocatorResponse(uri, 1, 1);

        assertFalse(response.getThrowable().isPresent());
        assertEquals(uri, response.getUri());
        assertEquals(1, response.getRequestCode());
        assertEquals(1, response.getResultCode());
    }
}

package co.smartreceipts.android.autocomplete

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AutoCompleteResultTest {

    @Test
    fun toStringIsOverridden() {
        // We explicitly test this, because the adapters use the #toString values
        assertEquals("test", AutoCompleteResult("test", Any()).toString())
    }
}
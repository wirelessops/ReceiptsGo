package co.smartreceipts.android.identity.widget.login.model

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UiInputValidationIndicatorTest {

    @Test
    @Throws(Exception::class)
    fun getters() {
        val indicator1 = UiInputValidationIndicator("test1", true, true)
        assertEquals("test1", indicator1.message)
        assertTrue(indicator1.isEmailValid)
        assertTrue(indicator1.isPasswordValid)

        val indicator2 = UiInputValidationIndicator("test2", false, false)
        assertEquals("test2", indicator2.message)
        assertFalse(indicator2.isEmailValid)
        assertFalse(indicator2.isPasswordValid)
    }

}
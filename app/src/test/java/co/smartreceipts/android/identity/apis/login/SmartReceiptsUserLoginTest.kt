package co.smartreceipts.android.identity.apis.login

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SmartReceiptsUserLoginTest {

    @Test
    fun getters() {
        val login = SmartReceiptsUserLogin("email", "password")
        assertEquals(login.typeString, "login")
        assertEquals(login.email, "email")
        assertEquals(login.password, "password")
        assertEquals(login.loginType, LoginType.LogIn)
        assertNull(login.token)
    }
}
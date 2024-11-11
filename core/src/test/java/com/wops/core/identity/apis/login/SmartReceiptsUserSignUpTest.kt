package com.wops.core.identity.apis.login

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SmartReceiptsUserSignUpTest {

    @Test
    fun getters() {
        val login = SmartReceiptsUserSignUp("email", "password")
        assertNull(login.typeString)
        assertEquals(login.email, "email")
        assertEquals(login.password, "password")
        assertEquals(login.loginType, LoginType.SignUp)
        assertNull(login.token)
    }

}
package co.smartreceipts.android.identity.store

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import co.smartreceipts.core.identity.store.MutableIdentityStore
import dagger.Lazy
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MutableIdentityStoreTest {

    // Class under test
    lateinit var mutableIdentityStore: MutableIdentityStore

    lateinit var sharedPreferences: SharedPreferences

    @Mock
    internal var lazySharedPreferences: Lazy<SharedPreferences>? = null

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
        `when`(lazySharedPreferences!!.get()).thenReturn(sharedPreferences)
        mutableIdentityStore = MutableIdentityStore(lazySharedPreferences!!)
    }

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().apply()
    }

    @Test
    fun defaultValues() {
        assertEquals(false, mutableIdentityStore.isLoggedIn)
        assertNull(mutableIdentityStore.email)
        assertNull(mutableIdentityStore.userId)
        assertNull(mutableIdentityStore.token)
    }

    @Test
    fun setCredentials() {
        val email = "test@test.com"
        val userId = "userId"
        val token = "token"
        mutableIdentityStore.setCredentials(email, userId, token)

        assertEquals(true, mutableIdentityStore.isLoggedIn)
        assertNotNull(mutableIdentityStore.email)
        assertNotNull(mutableIdentityStore.userId)
        assertNotNull(mutableIdentityStore.token)
        assertEquals(email, mutableIdentityStore.email!!.id)
        assertEquals(userId, mutableIdentityStore.userId!!.id)
        assertEquals(token, mutableIdentityStore.token!!.id)
    }

    @Test
    fun setLegacyCredentialsWithoutUserId() {
        val email = "test@test.com"
        val token = "token"
        mutableIdentityStore.setCredentials(email, null, token)

        assertEquals(true, mutableIdentityStore.isLoggedIn)
        assertNotNull(mutableIdentityStore.email)
        assertNotNull(mutableIdentityStore.token)
        assertEquals(email, mutableIdentityStore.email!!.id)
        assertEquals(token, mutableIdentityStore.token!!.id)
        assertNull(mutableIdentityStore.userId)
    }

    @Test
    fun nullOutCredentials() {
        val email = "test@test.com"
        val userId = "userId"
        val token = "token"
        mutableIdentityStore.setCredentials(email, userId, token)

        // Now null out
        mutableIdentityStore.setCredentials(null, null, null)

        assertEquals(false, mutableIdentityStore.isLoggedIn)
        assertNull(mutableIdentityStore.email)
        assertNull(mutableIdentityStore.userId)
        assertNull(mutableIdentityStore.token)
    }

}
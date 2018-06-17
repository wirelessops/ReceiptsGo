package co.smartreceipts.android.identity.store;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import dagger.Lazy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MutableIdentityStoreTest {

    // Class under test
    MutableIdentityStore mutableIdentityStore;

    SharedPreferences sharedPreferences;

    @Mock
    Lazy<SharedPreferences> lazySharedPreferences;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        when(lazySharedPreferences.get()).thenReturn(sharedPreferences);
        mutableIdentityStore = new MutableIdentityStore(lazySharedPreferences);
    }

    @After
    public void tearDown() {
        sharedPreferences.edit().clear().apply();
    }

    @Test
    public void defaultValues() {
        assertEquals(false, mutableIdentityStore.isLoggedIn());
        assertNull(mutableIdentityStore.getEmail());
        assertNull(mutableIdentityStore.getUserId());
        assertNull(mutableIdentityStore.getToken());
    }

    @Test
    public void setCredentials() {
        final String email = "test@test.com";
        final String userId = "userId";
        final String token = "token";
        mutableIdentityStore.setCredentials(email, userId, token);

        assertEquals(true, mutableIdentityStore.isLoggedIn());
        assertNotNull(mutableIdentityStore.getEmail());
        assertNotNull(mutableIdentityStore.getUserId());
        assertNotNull(mutableIdentityStore.getToken());
        assertEquals(email, mutableIdentityStore.getEmail().getId());
        assertEquals(userId, mutableIdentityStore.getUserId().getId());
        assertEquals(token, mutableIdentityStore.getToken().getId());
    }

    @Test
    public void setLegacyCredentialsWithoutUserId() {
        final String email = "test@test.com";
        final String token = "token";
        mutableIdentityStore.setCredentials(email, null, token);

        assertEquals(true, mutableIdentityStore.isLoggedIn());
        assertNotNull(mutableIdentityStore.getEmail());
        assertNotNull(mutableIdentityStore.getToken());
        assertEquals(email, mutableIdentityStore.getEmail().getId());
        assertEquals(token, mutableIdentityStore.getToken().getId());
        assertNull(mutableIdentityStore.getUserId());
    }

    @Test
    public void nullOutCredentials() {
        final String email = "test@test.com";
        final String userId = "userId";
        final String token = "token";
        mutableIdentityStore.setCredentials(email, userId, token);

        // Now null out
        mutableIdentityStore.setCredentials(null, null, null);

        assertEquals(false, mutableIdentityStore.isLoggedIn());
        assertNull(mutableIdentityStore.getEmail());
        assertNull(mutableIdentityStore.getUserId());
        assertNull(mutableIdentityStore.getToken());
    }

}
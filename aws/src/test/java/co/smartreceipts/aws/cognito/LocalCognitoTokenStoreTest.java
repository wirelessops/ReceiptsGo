package co.smartreceipts.aws.cognito;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;

import dagger.Lazy;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class LocalCognitoTokenStoreTest {

    private static final String TOKEN = "token";
    private static final String IDENTITY_ID = "identityId";
    private static final Date EXPIRES_AT = new Date(5);

    // Class under test
    LocalCognitoTokenStore localCognitoTokenStore;

    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext());

    @Mock
    Lazy<SharedPreferences> lazy;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(lazy.get()).thenReturn(preferences);

        localCognitoTokenStore = new LocalCognitoTokenStore(lazy);
    }

    @After
    public void tearDown() {
        preferences.edit().clear().apply();
    }

    @Test
    public void nothingPersisted() {
        assertEquals(null, localCognitoTokenStore.getCognitoToken());
    }

    @Test
    public void persist() {
        final Cognito cognito = new Cognito(TOKEN, IDENTITY_ID, EXPIRES_AT);
        localCognitoTokenStore.persist(cognito);
        assertEquals(cognito, localCognitoTokenStore.getCognitoToken());

        localCognitoTokenStore.persist(null);
        assertEquals(null, localCognitoTokenStore.getCognitoToken());
    }

}
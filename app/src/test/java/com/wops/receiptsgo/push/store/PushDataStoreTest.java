package com.wops.receiptsgo.push.store;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import com.wops.push.store.PushDataStore;
import dagger.Lazy;
import io.reactivex.observers.TestObserver;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PushDataStoreTest {

    // Class under test
    PushDataStore pushDataStore;

    SharedPreferences sharedPreferences;

    @Mock
    Lazy<SharedPreferences> lazy;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext());

        when(lazy.get()).thenReturn(sharedPreferences);
        pushDataStore = new PushDataStore(lazy);
    }

    @After
    public void tearDown() {
        sharedPreferences.edit().clear().apply();
    }

    @Test
    public void isRemoteRefreshRequiredDefaultsToTrue() throws Exception {
        assertTrue(pushDataStore.isRemoteRefreshRequired());
        final TestObserver<Boolean> testObserver = pushDataStore.isRemoteRefreshRequiredSingle().test();
        testObserver.onNext(true);
        testObserver.assertComplete()
                .assertNoErrors();
    }

    @Test
    public void setRemoteRefreshRequired() throws Exception {
        pushDataStore.setRemoteRefreshRequired(false);
        assertFalse(pushDataStore.isRemoteRefreshRequired());
        final TestObserver<Boolean> testObserver = pushDataStore.isRemoteRefreshRequiredSingle().test();
        testObserver.onNext(false);
        testObserver.assertComplete()
                .assertNoErrors();
    }

}
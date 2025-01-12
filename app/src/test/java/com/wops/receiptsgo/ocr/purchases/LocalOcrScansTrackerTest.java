package com.wops.receiptsgo.ocr.purchases;

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

import dagger.Lazy;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class LocalOcrScansTrackerTest {

    // Class under test
    LocalOcrScansTracker localOcrScansTracker;

    SharedPreferences preferences;

    @Mock
    Lazy<SharedPreferences> lazy;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext());

        when(lazy.get()).thenReturn(preferences);
        localOcrScansTracker = new LocalOcrScansTracker(lazy, Schedulers.trampoline());
    }

    @After
    public void tearDown() {
        preferences.edit().clear().apply();
    }

    @Test
    public void getRemainingScans() {
        TestObserver<Integer> testObserver = localOcrScansTracker.getRemainingScansStream().test();
        assertEquals(0, localOcrScansTracker.getRemainingScans());

        testObserver.assertValues(0);
        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void setRemainingScans() {
        TestObserver<Integer> testObserver = localOcrScansTracker.getRemainingScansStream().test();

        localOcrScansTracker.setRemainingScans(50);
        assertEquals(50, localOcrScansTracker.getRemainingScans());

        testObserver.assertValues(0, 50);
        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void decrementRemainingScans() {
        TestObserver<Integer> testObserver = localOcrScansTracker.getRemainingScansStream().test();

        localOcrScansTracker.setRemainingScans(50);
        localOcrScansTracker.decrementRemainingScans();
        assertEquals(49, localOcrScansTracker.getRemainingScans());

        testObserver.assertValues(0, 50, 49);
        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void decrementRemainingDoesntGoNegative() {
        TestObserver<Integer> testObserver = localOcrScansTracker.getRemainingScansStream().test();

        localOcrScansTracker.decrementRemainingScans();
        assertEquals(0, localOcrScansTracker.getRemainingScans());

        testObserver.assertValues(0);
        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
    }

}
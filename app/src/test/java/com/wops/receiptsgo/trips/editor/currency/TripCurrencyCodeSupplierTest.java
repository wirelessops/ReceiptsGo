package com.wops.receiptsgo.trips.editor.currency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TripCurrencyCodeSupplierTest {

    @Mock
    UserPreferenceManager userPreferenceManager;

    @Mock
    Trip trip;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void get() throws Exception {
        when(userPreferenceManager.get(UserPreference.General.DefaultCurrency)).thenReturn("prefs");
        when(trip.getDefaultCurrencyCode()).thenReturn("trip");
        final TripCurrencyCodeSupplier nullTripSupplier = new TripCurrencyCodeSupplier(userPreferenceManager, null);
        final TripCurrencyCodeSupplier validTripSupplier = new TripCurrencyCodeSupplier(userPreferenceManager, trip);
        assertEquals(nullTripSupplier.get(), "prefs");
        assertEquals(validTripSupplier.get(), "trip");
    }

}
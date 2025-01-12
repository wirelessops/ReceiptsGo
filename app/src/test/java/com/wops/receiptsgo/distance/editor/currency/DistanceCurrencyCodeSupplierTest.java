package com.wops.receiptsgo.distance.editor.currency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.model.Price;
import com.wops.receiptsgo.model.Trip;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DistanceCurrencyCodeSupplierTest {

    @Mock
    Trip trip;

    @Mock
    Distance distance;

    @Mock
    Price price;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(distance.getPrice()).thenReturn(price);
    }

    @Test
    public void get() throws Exception {
        when(trip.getDefaultCurrencyCode()).thenReturn("trip");
        when(price.getCurrencyCode()).thenReturn("distance");
        final DistanceCurrencyCodeSupplier nullDistanceSupplier = new DistanceCurrencyCodeSupplier(trip, null);
        final DistanceCurrencyCodeSupplier validDistanceSupplier = new DistanceCurrencyCodeSupplier(trip, distance);
        assertEquals(nullDistanceSupplier.get(), "trip");
        assertEquals(validDistanceSupplier.get(), "distance");
    }

}
package co.smartreceipts.android.persistence.database.tables.keys;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.tables.TripsTable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TripPrimaryKeyTest {

    // Class under test
    TripPrimaryKey mTripPrimaryKey;

    @Mock
    Trip mTrip;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mTripPrimaryKey = new TripPrimaryKey();
    }

    @Test
    public void getPrimaryKeyColumn() throws Exception {
        assertEquals(TripsTable.COLUMN_ID, mTripPrimaryKey.getPrimaryKeyColumn());
    }

    @Test
    public void getPrimaryKeyClass() throws Exception {
        assertEquals(Integer.class, mTripPrimaryKey.getPrimaryKeyClass());
    }

    @Test
    public void getPrimaryKeyValue() throws Exception {
        final int id = 5;
        when(mTrip.getId()).thenReturn(5);
        assertTrue(id == mTripPrimaryKey.getPrimaryKeyValue(mTrip));
    }
}
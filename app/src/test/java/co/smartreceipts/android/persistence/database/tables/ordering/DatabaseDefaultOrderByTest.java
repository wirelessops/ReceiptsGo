package co.smartreceipts.android.persistence.database.tables.ordering;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class DatabaseDefaultOrderByTest {

    @Test
    public void getOrderByPredicate() {
        final OrderByDatabaseDefault databaseDefaultOrderBy = new OrderByDatabaseDefault();
        assertNull(databaseDefaultOrderBy.getOrderByPredicate());
        assertEquals(databaseDefaultOrderBy.toString(), databaseDefaultOrderBy.getOrderByPredicate());
    }

}
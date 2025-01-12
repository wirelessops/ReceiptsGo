package com.wops.receiptsgo.persistence.database.tables.ordering;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class OrderByColumnTest {

    @Test
    public void getOrderByPredicate() {
        final String column = "column";
        final OrderByColumn descending = new OrderByColumn(column, true);
        final OrderByColumn ascending = new OrderByColumn(column, false);
        final OrderByColumn defaultOrder = new OrderByColumn(null, true);

        assertEquals(column + " DESC", descending.getOrderByPredicate());
        assertEquals(column + " ASC", ascending.getOrderByPredicate());
        assertNull(defaultOrder.getOrderByPredicate());

        assertEquals(descending.toString(), descending.getOrderByPredicate());
        assertEquals(ascending.toString(), ascending.getOrderByPredicate());
        assertEquals(defaultOrder.toString(), defaultOrder.getOrderByPredicate());
    }

}
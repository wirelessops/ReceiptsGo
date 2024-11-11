package com.wops.receiptsgo.model.comparators;

import java.util.Comparator;

import com.wops.receiptsgo.model.Receipt;

/**
 * An implementation of the {@link Comparator} interface, which checks if {@link com.wops.receiptsgo.model.Receipt#getDate()}
 * occurs before or after another receipt date. This can be used in conjunction with {@link java.util.Collections#sort(java.util.List)}.
 *
 * @author williambaumann
 */
public class ReceiptDateComparator implements Comparator<Receipt> {

    private final boolean mIsAscending;

    /**
     * Default constructor for this class, which uses ascending order for comparisons.
     */
    public ReceiptDateComparator() {
        this(true);
    }

    /**
     * Secondary constructor for this class, which allows manual specification for whether the resultant comparisons will produce
     * an ascending or descending {@link java.util.Collection}.
     *
     * @param isAscending - {@code true} if we should use ascending order. {@code false} for descending.
     */
    public ReceiptDateComparator(boolean isAscending) {
        mIsAscending = isAscending;
    }


    @Override
    public int compare(Receipt first, Receipt second) {
        if (first == null && second == null) {
            return 0;
        } else if (first == null) {
            return Integer.MIN_VALUE;
        } else if (second == null) {
            return Integer.MAX_VALUE;
        } else {
            return first.getDate().compareTo(second.getDate()) * (mIsAscending ? 1 : -1);
        }
    }
}

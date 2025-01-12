package com.wops.receiptsgo.utils.sorting;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class AlphabeticalCaseInsensitiveCharSequenceComparatorTest {

    @Test
    public void compare() {
        final AlphabeticalCaseInsensitiveCharSequenceComparator comparator = new AlphabeticalCaseInsensitiveCharSequenceComparator();
        final String string1 = "abcde";
        final String string2 = "wxyz";
        assertTrue(comparator.compare(string1, string1) == 0);
        assertTrue(comparator.compare(string2, string2) == 0);
        assertTrue(comparator.compare(string1, string2) < 0);
        assertTrue(comparator.compare(string2, string1) > 0);
    }
}
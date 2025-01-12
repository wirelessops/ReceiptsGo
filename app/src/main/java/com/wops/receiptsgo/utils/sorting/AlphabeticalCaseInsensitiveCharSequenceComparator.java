package com.wops.receiptsgo.utils.sorting;

import java.util.Comparator;

public class AlphabeticalCaseInsensitiveCharSequenceComparator implements Comparator<CharSequence> {

    @Override
    public int compare(CharSequence charSequence1, CharSequence charSequence2) {
        return charSequence1.toString().compareToIgnoreCase(charSequence2.toString());
    }
}

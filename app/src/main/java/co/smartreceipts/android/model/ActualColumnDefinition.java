package co.smartreceipts.android.model;

import android.support.annotation.StringRes;

public interface ActualColumnDefinition {

    int getColumnType();

    @StringRes
    int getColumnHeaderId();
}

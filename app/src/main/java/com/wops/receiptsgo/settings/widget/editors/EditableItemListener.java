package com.wops.receiptsgo.settings.widget.editors;

import androidx.annotation.Nullable;

public interface EditableItemListener<T> {

    void onEditItem(T oldItem, @Nullable T newItem);

    void onDeleteItem(T item);
}

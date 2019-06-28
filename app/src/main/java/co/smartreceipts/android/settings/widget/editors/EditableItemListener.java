package co.smartreceipts.android.settings.widget.editors;

import androidx.annotation.Nullable;

public interface EditableItemListener<T> {

    void onEditItem(T oldItem, @Nullable T newItem);

    void onDeleteItem(T item);
}

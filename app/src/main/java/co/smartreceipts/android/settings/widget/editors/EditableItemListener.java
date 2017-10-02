package co.smartreceipts.android.settings.widget.editors;

public interface EditableItemListener<T> {

    void onEditItem(T item);

    void onDeleteItem(T item);
}

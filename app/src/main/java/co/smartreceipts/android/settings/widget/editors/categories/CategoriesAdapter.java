package co.smartreceipts.android.settings.widget.editors.categories;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.settings.widget.editors.EditableItemListener;
import co.smartreceipts.android.settings.widget.editors.adapters.EditableCardsAdapter;

public class CategoriesAdapter extends EditableCardsAdapter<Category> {

    public CategoriesAdapter(EditableItemListener<Category> listener) {
        super(listener);
    }

    @Override
    public void onBindViewHolder(EditableCardsViewHolder holder, int position) {
       Category category = items.get(position);

        holder.title.setText(category.getName());
        holder.summary.setText(category.getCode());

        holder.edit.setOnClickListener(v -> listener.onEditItem(category));
        holder.delete.setOnClickListener(v -> listener.onDeleteItem(category));
    }

    @Override
    public long getItemId(int position) {
        // TODO: 30.09.2017 deal with IDs
        return position;
    }
}

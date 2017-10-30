package co.smartreceipts.android.settings.widget.editors.categories;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.settings.widget.editors.EditableItemListener;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableEditableCardsAdapter;
import co.smartreceipts.android.utils.log.Logger;

public class CategoriesAdapter extends DraggableEditableCardsAdapter<Category> {

    public CategoriesAdapter(EditableItemListener<Category> listener) {
        super(listener);
    }

    @Override
    public void onBindViewHolder(EditableCardsViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Category category = items.get(position);

        holder.title.setText(category.getName());
        holder.summary.setText(category.getCode());

        holder.edit.setOnClickListener(v -> listener.onEditItem(category));
        holder.delete.setOnClickListener(v -> listener.onDeleteItem(category));

        holder.testOrderNumber.setText(String.valueOf(category.getCustomOrderId()));
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public void saveNewOrder(TableController<Category> tableController) {
        Logger.debug(this, "saveNewOrder Categories");
        for (Category item : items) {
            if (item.getCustomOrderId() != items.indexOf(item)) {
                tableController.update(item, new CategoryBuilderFactory()
                                .setId(item.getId())
                                .setName(item.getName())
                                .setCode(item.getCode())
                                .setSyncState(item.getSyncState())
                                .setCustomOrderId(items.indexOf(item))
                                .build(),
                        new DatabaseOperationMetadata());
            }
        }
    }

}

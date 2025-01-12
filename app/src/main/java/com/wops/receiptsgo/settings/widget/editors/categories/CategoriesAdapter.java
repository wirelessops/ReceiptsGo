package com.wops.receiptsgo.settings.widget.editors.categories;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Category;
import com.wops.receiptsgo.model.factory.CategoryBuilderFactory;
import com.wops.receiptsgo.persistence.database.controllers.TableController;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.settings.widget.editors.EditableItemListener;
import com.wops.receiptsgo.settings.widget.editors.adapters.DraggableEditableCardsAdapter;

public class CategoriesAdapter extends DraggableEditableCardsAdapter<Category> {

    CategoriesAdapter(EditableItemListener<Category> listener) {
        super(listener);
    }

    @Override
    public AbstractDraggableItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dragable_editable_card, parent, false);
        return new CategoryViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(AbstractDraggableItemViewHolder holder, int position) {
        CategoryViewHolder categoryHolder = (CategoryViewHolder) holder;
        Category category = items.get(position);

        categoryHolder.dragHandle.setVisibility(isOnDragMode ? View.VISIBLE : View.GONE);
        categoryHolder.delete.setVisibility(isOnDragMode ? View.GONE : View.VISIBLE);
        categoryHolder.edit.setVisibility(isOnDragMode ? View.GONE : View.VISIBLE);
        categoryHolder.divider.setVisibility(isOnDragMode ? View.GONE : View.VISIBLE);

        categoryHolder.categoryName.setText(category.getName());
        categoryHolder.categoryCode.setText(category.getCode());

        categoryHolder.edit.setOnClickListener(v -> listener.onEditItem(category, null));
        categoryHolder.delete.setOnClickListener(v -> listener.onDeleteItem(category));
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public void saveNewOrder(TableController<Category> tableController) {
        for (Category item : items) {
            if (item.getCustomOrderId() != items.indexOf(item)) {
                tableController.update(item, new CategoryBuilderFactory(item)
                                .setCustomOrderId(items.indexOf(item))
                                .build(),
                        new DatabaseOperationMetadata());
            }
        }
    }

    private static class CategoryViewHolder extends AbstractDraggableItemViewHolder {

        TextView categoryName;
        TextView categoryCode;
        public View edit;
        public View delete;
        View dragHandle;
        View divider;

        CategoryViewHolder(View itemView) {
            super(itemView);

            categoryName = itemView.findViewById(android.R.id.title);
            categoryCode = itemView.findViewById(android.R.id.summary);
            edit = itemView.findViewById(R.id.edit);
            delete = itemView.findViewById(R.id.delete);
            dragHandle = itemView.findViewById(R.id.drag_handle);
            divider = itemView.findViewById(R.id.divider);
        }
    }

}

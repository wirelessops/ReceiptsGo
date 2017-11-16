package co.smartreceipts.android.settings.widget.editors.categories;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.settings.widget.editors.EditableItemListener;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableEditableCardsAdapter;
import co.smartreceipts.android.utils.log.Logger;

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

        categoryHolder.edit.setOnClickListener(v -> listener.onEditItem(category));
        categoryHolder.delete.setOnClickListener(v -> listener.onDeleteItem(category));
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

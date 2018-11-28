package co.smartreceipts.android.settings.widget.editors.categories;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.impl.CategoriesTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.settings.widget.editors.DraggableEditableListFragment;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableEditableCardsAdapter;
import dagger.android.support.AndroidSupportInjection;

public class CategoriesListFragment extends DraggableEditableListFragment<Category> {

    public static String TAG = "CategoriesListFragment";

    @Inject
    CategoriesTableController categoriesTableController;

    @Inject
    OrderingPreferencesManager orderingPreferencesManager;

    public static CategoriesListFragment newInstance() {
        return new CategoriesListFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.menu_main_categories);
            actionBar.setSubtitle(null);
        }
    }

    protected DraggableEditableCardsAdapter<Category> getAdapter() {
        return new CategoriesAdapter(this);
    }

    @Override
    protected TableController<Category> getTableController() {
        return categoriesTableController;
    }

    @Override
    protected void addItem() {
        showCreateEditDialog(null);
    }

    @Override
    public void onEditItem(Category oldItem, @Nullable Category ignored) {
        showCreateEditDialog(oldItem);
    }

    @Override
    public void onDeleteItem(Category category) {
        final AlertDialog.Builder innerBuilder = new AlertDialog.Builder(getActivity());
        innerBuilder.setTitle(getString(R.string.delete_item, category.getName()))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> categoriesTableController.delete(category, new DatabaseOperationMetadata()))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
    }

    @Override
    protected void saveTableOrdering() {
        super.saveTableOrdering();
        orderingPreferencesManager.saveCategoriesTableOrdering();
    }

    private void showCreateEditDialog(@Nullable Category editCategory) {

        boolean isEdit = editCategory != null;

        final android.support.v7.app.AlertDialog.Builder innerBuilder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        final LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.BOTTOM);
        layout.setPadding(6, 6, 6, 6);
        final TextView nameLabel = new TextView(getActivity());
        nameLabel.setText(R.string.item_name);
        final EditText nameBox = new EditText(getActivity());
        nameBox.setText(isEdit ? editCategory.getName() : "");
        final TextView codeLabel = new TextView(getActivity());
        codeLabel.setText(R.string.item_code);
        final EditText codeBox = new EditText(getActivity());
        codeBox.setText(isEdit ? editCategory.getCode() : "");
        layout.addView(nameLabel);
        layout.addView(nameBox);
        layout.addView(codeLabel);
        layout.addView(codeBox);
        innerBuilder.setTitle(isEdit ? R.string.dialog_category_edit : R.string.dialog_category_add)
                .setView(layout)
                .setCancelable(true)
                .setPositiveButton(isEdit ? R.string.update : R.string.add, (dialog, which) -> {
                    final String newName = nameBox.getText().toString();
                    final String newCode = codeBox.getText().toString();

                    final CategoryBuilderFactory categoryBuilderFactory = new CategoryBuilderFactory()
                            .setName(newName)
                            .setCode(newCode)
                            .setCustomOrderId(isEdit ? editCategory.getCustomOrderId() : Long.MAX_VALUE);
                    if (isEdit) {
                        categoryBuilderFactory.setUuid(editCategory.getUuid());
                    }
                    final Category category = categoryBuilderFactory.build();

                    if (isEdit) {
                        categoriesTableController.update(editCategory, category, new DatabaseOperationMetadata());
                    } else {
                        categoriesTableController.insert(category, new DatabaseOperationMetadata());
                        scrollToEnd();
                    }

                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
    }
}
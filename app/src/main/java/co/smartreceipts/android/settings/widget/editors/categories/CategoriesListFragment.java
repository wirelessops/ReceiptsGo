package co.smartreceipts.android.settings.widget.editors.categories;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Category;
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
    public void onInsertSuccess(@NonNull Category category, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        super.onInsertSuccess(category, databaseOperationMetadata);
        scrollToEnd();
    }

    @Override
    protected void saveTableOrdering() {
        super.saveTableOrdering();
        orderingPreferencesManager.saveCategoriesTableOrdering();
    }

    private void showCreateEditDialog(@Nullable Category editCategory) {
        CategoryEditorDialogFragment.newInstance(editCategory).show(requireFragmentManager(), CategoryEditorDialogFragment.TAG);
    }
}
package co.smartreceipts.android.settings.widget.editors.columns;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.impl.CSVTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.settings.widget.editors.DraggableEditableListFragment;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableEditableCardsAdapter;
import dagger.android.support.AndroidSupportInjection;

public class CSVColumnsListFragment extends DraggableEditableListFragment<Column<Receipt>> {

    public static String TAG = "CSVColumnsListFragment";

    @Inject
    ReceiptColumnDefinitions receiptColumnDefinitions;
    @Inject
    CSVTableController csvTableController;
    @Inject
    OrderingPreferencesManager orderingPreferencesManager;

    public static CSVColumnsListFragment newInstance() {
        return new CSVColumnsListFragment();
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
            actionBar.setTitle(R.string.menu_main_csv);
        }
    }

    @Override
    protected DraggableEditableCardsAdapter<Column<Receipt>> getAdapter() {
        return new ColumnsAdapter(this, receiptColumnDefinitions, getContext());
    }

    @Override
    protected TableController<Column<Receipt>> getTableController() {
        return csvTableController;
    }

    @Override
    protected void addItem() {
        csvTableController.insertDefaultColumn();
        scrollToEnd();
    }

    @Override
    public void onEditItem(Column<Receipt> oldItem, @Nullable Column<Receipt> newItem) {
        if (newItem != null) {
            csvTableController.update(oldItem, newItem, new DatabaseOperationMetadata());
        } else {
            throw new IllegalArgumentException("New column must not be null");
        }
    }

    @Override
    public void onDeleteItem(Column<Receipt> item) {
        final AlertDialog.Builder innerBuilder = new AlertDialog.Builder(getActivity());
        innerBuilder.setTitle(getString(R.string.delete_item, item.getName()))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> csvTableController.delete(item, new DatabaseOperationMetadata()))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
    }

    @Override
    protected void saveTableOrdering() {
        super.saveTableOrdering();
        orderingPreferencesManager.saveCsvColumnsTableOrdering();
    }
}

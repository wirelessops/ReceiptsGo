package com.wops.receiptsgo.settings.widget.editors.columns;

import android.app.AlertDialog;
import androidx.annotation.Nullable;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions;
import com.wops.receiptsgo.persistence.database.controllers.impl.ColumnTableController;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.settings.widget.editors.DraggableEditableListFragment;
import com.wops.receiptsgo.settings.widget.editors.adapters.DraggableEditableCardsAdapter;
import com.wops.receiptsgo.workers.reports.ReportResourcesManager;

public abstract class ColumnsListFragment extends DraggableEditableListFragment<Column<Receipt>> {

    @Override
    public void onEditItem(Column<Receipt> oldItem, @Nullable Column<Receipt> newItem) {
        if (newItem != null) {
            getTableController().update(oldItem, newItem, new DatabaseOperationMetadata());
        } else {
            throw new IllegalArgumentException("New column must not be null");
        }
    }

    @Override
    public void onDeleteItem(Column<Receipt> item) {
        final AlertDialog.Builder innerBuilder = new AlertDialog.Builder(getActivity());
        innerBuilder.setTitle(getString(R.string.delete_item, getReportResourcesManager().getFlexString(item.getHeaderStringResId())))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> getTableController().delete(item, new DatabaseOperationMetadata()))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
    }

    @Override
    protected DraggableEditableCardsAdapter<Column<Receipt>> getAdapter() {
        return new ColumnsAdapter(getContext(), getReceiptColumnDefinitions(), getReportResourcesManager(), this);
    }

    @Override
    protected void addItem() {
        ((ColumnTableController)getTableController()).insertDefaultColumn();
        scrollToEnd();
    }

    protected abstract ReceiptColumnDefinitions getReceiptColumnDefinitions();

    protected abstract ReportResourcesManager getReportResourcesManager();

}

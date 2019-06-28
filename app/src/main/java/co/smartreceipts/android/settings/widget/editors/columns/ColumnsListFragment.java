package co.smartreceipts.android.settings.widget.editors.columns;

import android.app.AlertDialog;
import androidx.annotation.Nullable;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.database.controllers.impl.ColumnTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.settings.widget.editors.DraggableEditableListFragment;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableEditableCardsAdapter;
import co.smartreceipts.android.workers.reports.ReportResourcesManager;

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

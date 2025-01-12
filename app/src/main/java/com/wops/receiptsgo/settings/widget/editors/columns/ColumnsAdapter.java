package com.wops.receiptsgo.settings.widget.editors.columns;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.factory.ColumnBuilderFactory;
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions;
import com.wops.receiptsgo.persistence.database.controllers.TableController;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.settings.widget.editors.EditableItemListener;
import com.wops.receiptsgo.settings.widget.editors.adapters.DraggableEditableCardsAdapter;
import com.wops.receiptsgo.widget.UserSelectionTrackingOnItemSelectedListener;
import com.wops.receiptsgo.widget.ui.OutlinedSpinner;
import com.wops.receiptsgo.workers.reports.ReportResourcesManager;

public class ColumnsAdapter extends DraggableEditableCardsAdapter<Column<Receipt>> {

    private ColumnsSpinnerAdapter<Receipt> spinnerAdapter;
    private Context context;
    private ReceiptColumnDefinitions receiptColumnDefinitions;


    ColumnsAdapter(Context context, ReceiptColumnDefinitions columnDefinitions, ReportResourcesManager reportResourcesManager,
                   EditableItemListener<Column<Receipt>> listener) {
        super(listener);

        this.context = context;
        this.receiptColumnDefinitions = columnDefinitions;

        spinnerAdapter = new ColumnsSpinnerAdapter<>(reportResourcesManager, columnDefinitions.getAllColumns());
    }

    @Override
    @NonNull
    public AbstractDraggableItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_draggable_spinner_card, parent, false);
        return new ColumnViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractDraggableItemViewHolder holder, int position) {
        ColumnViewHolder columnHolder = (ColumnViewHolder) holder;

        columnHolder.dragHandle.setVisibility(isOnDragMode ? View.VISIBLE : View.GONE);
        columnHolder.delete.setVisibility(isOnDragMode ? View.GONE : View.VISIBLE);
        columnHolder.outlinedSpinner.setEnabled(!isOnDragMode);

        columnHolder.outlinedSpinner.get().setAdapter(spinnerAdapter);

        columnHolder.outlinedSpinner.setCaptionText(context.getString(R.string.column_item, Integer.toString(position + 1))); //Add +1 to make it not 0-th index
        final int selectedPosition = getSpinnerPositionByColumnType(position);
        if (selectedPosition >= 0) {
            columnHolder.outlinedSpinner.get().setSelection(selectedPosition);
        }
        columnHolder.outlinedSpinner.get().setOnItemSelectedListener(new ColumnTypeChangeSelectionListener());
        SpinnerTag spinnerTag = (SpinnerTag) columnHolder.outlinedSpinner.get().getTag();
        spinnerTag.column = items.get(position);

        columnHolder.delete.setOnClickListener(v -> listener.onDeleteItem(items.get(position)));
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public void saveNewOrder(TableController<Column<Receipt>> tableController) {

        for (Column<Receipt> column : items) {
            if (column.getCustomOrderId() != items.indexOf(column)) {
                tableController.update(column, new ColumnBuilderFactory<>(receiptColumnDefinitions)
                        .setColumnId(column.getId())
                        .setColumnType(column.getType())
                        .setSyncState(column.getSyncState())
                        .setCustomOrderId(items.indexOf(column))
                        .build(), new DatabaseOperationMetadata());
            }
        }
    }

    /**
     * Attempts to get the position in the spinner based on the column type. Since column "equals"
     * also takes into account the actual position in the database, we do a pseudo equals here by
     * type
     *
     * @param itemPosition the position of the column
     * @return the position in the spinner or -1 if unknown
     */
    private int getSpinnerPositionByColumnType(int itemPosition) {
        final int columnType = items.get(itemPosition).getType();
        for (int i = 0; i < spinnerAdapter.getCount(); i++) {
            if (columnType == spinnerAdapter.getItem(i).getType()) {
                return i;
            }
        }
        return -1;
    }


    private static class ColumnViewHolder extends AbstractDraggableItemViewHolder {

        public OutlinedSpinner outlinedSpinner;
        public View delete;
        View dragHandle;

        ColumnViewHolder(View itemView) {
            super(itemView);

            outlinedSpinner = itemView.findViewById(R.id.column_spinner);
            delete = itemView.findViewById(R.id.delete);
            dragHandle = itemView.findViewById(R.id.drag_handle);

            outlinedSpinner.get().setTag(new SpinnerTag());
        }
    }

    private class ColumnTypeChangeSelectionListener extends UserSelectionTrackingOnItemSelectedListener {

        @Override
        public void onUserSelectedNewItem(AdapterView<?> parent, View view, int position, long id, int previousPosition) {
            final SpinnerTag spinnerTag = (SpinnerTag) parent.getTag();
            final Column<Receipt> oldColumn = spinnerTag.column;
            final Column<Receipt> newColumn = spinnerAdapter.getItem(position);

            listener.onEditItem(oldColumn, new ColumnBuilderFactory<>(receiptColumnDefinitions)
                    .setColumnType(newColumn.getType())
                    .setColumnId(oldColumn.getId())
                    .setSyncState(oldColumn.getSyncState())
                    .setCustomOrderId(oldColumn.getCustomOrderId())
                    .build()
            );

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            /* no-op */
        }
    }

    private static final class SpinnerTag {
        public Column<Receipt> column;
    }
}

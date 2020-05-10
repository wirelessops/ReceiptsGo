package co.smartreceipts.android.settings.widget.editors.payment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.settings.widget.editors.EditableItemListener;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableEditableCardsAdapter;
import co.smartreceipts.analytics.log.Logger;

public class PaymentMethodsAdapter extends DraggableEditableCardsAdapter<PaymentMethod> {

    PaymentMethodsAdapter(EditableItemListener<PaymentMethod> listener) {
        super(listener);
    }

    @Override
    @NonNull
    public AbstractDraggableItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dragable_editable_card, parent, false);
        return new PaymentMethodViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractDraggableItemViewHolder holder, int position) {
        PaymentMethodViewHolder paymentMethodHolder = (PaymentMethodViewHolder) holder;
        PaymentMethod method = items.get(position);

        paymentMethodHolder.dragHandle.setVisibility(isOnDragMode ? View.VISIBLE : View.GONE);
        paymentMethodHolder.delete.setVisibility(isOnDragMode ? View.GONE : View.VISIBLE);
        paymentMethodHolder.edit.setVisibility(isOnDragMode ? View.GONE : View.VISIBLE);
        paymentMethodHolder.divider.setVisibility(isOnDragMode ? View.GONE : View.VISIBLE);

        paymentMethodHolder.paymentMethodName.setText(method.getMethod());

        if (method.isReimbursable()) {
            paymentMethodHolder.isReimbursable.setText(R.string.graphs_label_reimbursable);
        } else {
            paymentMethodHolder.isReimbursable.setText(R.string.graphs_label_non_reimbursable);
        }

        paymentMethodHolder.edit.setOnClickListener(v -> listener.onEditItem(method, null));
        paymentMethodHolder.delete.setOnClickListener(v -> listener.onDeleteItem(method));
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public void saveNewOrder(TableController<PaymentMethod> tableController) {
        Logger.debug(this, "saveNewOrder Categories");

        for (PaymentMethod item : items) {
            tableController.update(item, new PaymentMethodBuilderFactory()
                            .setId(item.getId())
                            .setMethod(item.getMethod())
                            .setSyncState(item.getSyncState())
                            .setCustomOrderId(items.indexOf(item))
                            .setReimbursable(item.isReimbursable())
                            .build(),
                    new DatabaseOperationMetadata());
        }
    }

    private static class PaymentMethodViewHolder extends AbstractDraggableItemViewHolder {

        TextView paymentMethodName;
        TextView isReimbursable;
        public View edit;
        public View delete;
        View dragHandle;
        View divider;

        PaymentMethodViewHolder(View itemView) {
            super(itemView);

            paymentMethodName = itemView.findViewById(android.R.id.title);
            isReimbursable = itemView.findViewById(android.R.id.summary);
            edit = itemView.findViewById(R.id.edit);
            delete = itemView.findViewById(R.id.delete);
            dragHandle = itemView.findViewById(R.id.drag_handle);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}

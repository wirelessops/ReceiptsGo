package co.smartreceipts.android.settings.widget.editors.payment;

import android.view.View;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.settings.widget.editors.EditableItemListener;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableEditableCardsAdapter;
import co.smartreceipts.android.utils.log.Logger;

public class PaymentMethodsAdapter extends DraggableEditableCardsAdapter<PaymentMethod> {

    public PaymentMethodsAdapter(EditableItemListener<PaymentMethod> listener) {
        super(listener);
    }

    @Override
    public void onBindViewHolder(EditableCardsViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        PaymentMethod method = items.get(position);

        holder.title.setText(method.getMethod());
        holder.summary.setVisibility(View.GONE);

        holder.edit.setOnClickListener(v -> listener.onEditItem(method));
        holder.delete.setOnClickListener(v -> listener.onDeleteItem(method));

        holder.testOrderNumber.setText(String.valueOf(method.getCustomOrderId()));

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
                            .build(),
                    new DatabaseOperationMetadata());
        }
    }
}

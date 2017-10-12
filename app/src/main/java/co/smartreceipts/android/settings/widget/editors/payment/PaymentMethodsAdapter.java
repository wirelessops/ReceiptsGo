package co.smartreceipts.android.settings.widget.editors.payment;

import android.view.View;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.settings.widget.editors.EditableItemListener;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableEditableCardsAdapter;

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
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }
}

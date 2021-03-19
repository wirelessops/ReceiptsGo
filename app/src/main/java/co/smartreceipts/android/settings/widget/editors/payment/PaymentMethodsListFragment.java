package co.smartreceipts.android.settings.widget.editors.payment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.impl.PaymentMethodsTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.widget.editors.DraggableEditableListFragment;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableEditableCardsAdapter;
import dagger.android.support.AndroidSupportInjection;

public class PaymentMethodsListFragment extends DraggableEditableListFragment<PaymentMethod> {

    public static final String TAG = PaymentMethodsListFragment.class.getSimpleName();

    @Inject
    PaymentMethodsTableController paymentMethodsTableController;

    @Inject
    OrderingPreferencesManager orderingPreferencesManager;

    @Inject
    UserPreferenceManager userPreferenceManager;

    public static PaymentMethodsListFragment newInstance() {
        return new PaymentMethodsListFragment();
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
            getSupportActionBar().setTitle(R.string.payment_methods);
            getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // listening for result of create/edit dialog
        getChildFragmentManager().setFragmentResultListener(PaymentMethodEditorDialogFragment.REQUEST_KEY, this,
                (requestKey, result) -> {
                    @Nullable PaymentMethod editedMethod = result.getParcelable(PaymentMethod.PARCEL_KEY);
                    String methodName = result.getString(PaymentMethodEditorDialogFragment.RESULT_NAME_KEY);
                    boolean isReimbursable = result.getBoolean(PaymentMethodEditorDialogFragment.RESULT_IS_REIMBURSABLE_KEY);

                    if (editedMethod == null) { // add new payment method
                        final PaymentMethod paymentMethod = new PaymentMethodBuilderFactory()
                                .setMethod(methodName)
                                .setCustomOrderId(Long.MAX_VALUE)
                                .setReimbursable(isReimbursable)
                                .build();
                        getTableController().insert(paymentMethod, new DatabaseOperationMetadata());
                        scrollToEnd();
                    } else { // edit existing payment method
                        final PaymentMethod newPaymentMethod = new PaymentMethodBuilderFactory()
                                .setMethod(methodName)
                                .setCustomOrderId(editedMethod.getCustomOrderId())
                                .setReimbursable(isReimbursable)
                                .build();

                        getTableController().update(editedMethod, newPaymentMethod, new DatabaseOperationMetadata());
                    }
                });
    }

    @Override
    protected DraggableEditableCardsAdapter<PaymentMethod> getAdapter() {
        return new PaymentMethodsAdapter(this);
    }

    @Override
    protected TableController<PaymentMethod> getTableController() {
        return paymentMethodsTableController;
    }

    @Override
    protected void saveTableOrdering() {
        super.saveTableOrdering();
        orderingPreferencesManager.savePaymentMethodsTableOrdering();
    }

    @Override
    protected void addItem() {
        showCreateEditDialog(null);
    }

    @Override
    public void onEditItem(PaymentMethod oldPaymentMethod, @Nullable PaymentMethod ignored) {
        showCreateEditDialog(oldPaymentMethod);
    }

    @Override
    public void onDeleteItem(PaymentMethod item) {
        final DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                getTableController().delete(item, new DatabaseOperationMetadata());
            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.delete_item, item.getMethod()));
        builder.setPositiveButton(R.string.delete, onClickListener);
        builder.setNegativeButton(android.R.string.cancel, onClickListener);
        builder.show();
    }

    private void showCreateEditDialog(@Nullable PaymentMethod paymentMethod) {
        PaymentMethodEditorDialogFragment.newInstance(paymentMethod).show(getChildFragmentManager(), PaymentMethodEditorDialogFragment.TAG);
    }
}

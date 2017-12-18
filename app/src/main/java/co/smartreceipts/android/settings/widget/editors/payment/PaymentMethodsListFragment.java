package co.smartreceipts.android.settings.widget.editors.payment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.impl.PaymentMethodsTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.settings.widget.editors.DraggableEditableListFragment;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableEditableCardsAdapter;
import dagger.android.support.AndroidSupportInjection;
import wb.android.dialog.fragments.EditTextDialogFragment;

public class PaymentMethodsListFragment extends DraggableEditableListFragment<PaymentMethod> {

    public static final String TAG = PaymentMethodsListFragment.class.getSimpleName();

    @Inject
    PaymentMethodsTableController paymentMethodsTableController;

    @Inject
    OrderingPreferencesManager orderingPreferencesManager;

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
        final EditTextDialogFragment.OnClickListener onClickListener = (text, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                final PaymentMethod paymentMethod = new PaymentMethodBuilderFactory()
                        .setMethod(text)
                        .setCustomOrderId(Long.MAX_VALUE)
                        .build();
                getTableController().insert(paymentMethod, new DatabaseOperationMetadata());
                scrollToEnd();
            }
        };
        final String title = getString(R.string.payment_method_add);
        final String positiveButtonText = getString(R.string.add);
        showDialog(title, null, positiveButtonText, onClickListener);
    }

    private void showDialog(final String title, final String text, final String positiveButtonText, final EditTextDialogFragment.OnClickListener onClickListener) {
        final String negativeButtonText = getString(android.R.string.cancel);
        final String hint = getString(R.string.payment_method);
        if (getFragmentManager().findFragmentByTag(EditTextDialogFragment.TAG) == null) {
            final EditTextDialogFragment fragment = EditTextDialogFragment.newInstance(title, text, hint, positiveButtonText, negativeButtonText, onClickListener);
            fragment.show(getFragmentManager(), EditTextDialogFragment.TAG);
        }
    }

    @Override
    public void onEditItem(PaymentMethod oldPaymentMethod, @Nullable PaymentMethod ignored) {
        final EditTextDialogFragment.OnClickListener onClickListener = (text, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                final PaymentMethod newPaymentMethod = new PaymentMethodBuilderFactory()
                        .setMethod(text)
                        .setCustomOrderId(oldPaymentMethod.getCustomOrderId())
                        .build();

                getTableController().update(oldPaymentMethod, newPaymentMethod, new DatabaseOperationMetadata());
            }
        };
        final String title = getString(R.string.payment_method_edit);
        final String positiveButtonText = getString(R.string.save);
        showDialog(title, oldPaymentMethod.getMethod(), positiveButtonText, onClickListener);
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
}

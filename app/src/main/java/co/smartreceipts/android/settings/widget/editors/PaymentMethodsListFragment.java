package co.smartreceipts.android.settings.widget.editors;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.impl.PaymentMethodsTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import dagger.android.support.AndroidSupportInjection;
import wb.android.dialog.fragments.EditTextDialogFragment;

public class PaymentMethodsListFragment extends SimpleInsertableListFragment<PaymentMethod> implements View.OnClickListener {

	public static final String TAG = PaymentMethodsListFragment.class.getSimpleName();

	@Inject
	PaymentMethodsTableController paymentMethodsTableController;

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
	public void onClick(final View view) {
		if (view.getId() == R.id.edit) {
			editItem((PaymentMethod)view.getTag());
		}
		else if (view.getId() == R.id.delete) {
			deleteItem((PaymentMethod)view.getTag());
		}
	}

    @Override
    protected TableController<PaymentMethod> getTableController() {
        return paymentMethodsTableController;
    }

    @Override
	protected void addItem() {
		final EditTextDialogFragment.OnClickListener onClickListener = new EditTextDialogFragment.OnClickListener() {
			@Override
			public void onClick(String text, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					final PaymentMethod paymentMethod = new PaymentMethodBuilderFactory().setMethod(text).build();
					getTableController().insert(paymentMethod, new DatabaseOperationMetadata());
				}
			}
		};
		final String title = getString(R.string.payment_method_add);
		final String positiveButtonText = getString(R.string.add);
		showDialog(title, null, positiveButtonText, onClickListener);
	}
	
	protected void editItem(final PaymentMethod oldPaymentMethod) {
		final EditTextDialogFragment.OnClickListener onClickListener = new EditTextDialogFragment.OnClickListener() {
			@Override
			public void onClick(String text, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					final PaymentMethod newPaymentMethod = new PaymentMethodBuilderFactory().setMethod(text).build();
                    getTableController().update(oldPaymentMethod, newPaymentMethod, new DatabaseOperationMetadata());
				}
			}
		};
		final String title = getString(R.string.payment_method_edit);
		final String positiveButtonText = getString(R.string.save);
		showDialog(title, oldPaymentMethod.getMethod(), positiveButtonText, onClickListener);
	}
	
	private void showDialog(final String title, final String text, final String positiveButtonText, final EditTextDialogFragment.OnClickListener onClickListener) {
		final String negativeButtonText = getString(android.R.string.cancel);
		final String hint = getString(R.string.payment_method);
		if (getFragmentManager().findFragmentByTag(EditTextDialogFragment.TAG) == null) {
			final EditTextDialogFragment fragment = EditTextDialogFragment.newInstance(title, text, hint, positiveButtonText, negativeButtonText, onClickListener);
			fragment.show(getFragmentManager(), EditTextDialogFragment.TAG);
		}
	}
	
	protected void deleteItem(final PaymentMethod item) {
		final DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
                    getTableController().delete(item, new DatabaseOperationMetadata());
				}
			}
		};
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.delete_item, item.getMethod()));
		builder.setPositiveButton(R.string.delete, onClickListener);
		builder.setNegativeButton(android.R.string.cancel, onClickListener);
		builder.show();
	}

	private static final class ViewHolder {
		public TextView title;
		public View edit;
		public View delete;
	}
	
	@Override
	public View getView(final LayoutInflater inflater, final PaymentMethod item, View convertView, final ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.simple_editable_card_item, parent, false);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(android.R.id.title);
			holder.edit = convertView.findViewById(R.id.edit);
			holder.delete = convertView.findViewById(R.id.delete);
			holder.edit.setOnClickListener(this);
			holder.delete.setOnClickListener(this);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.title.setText(item.getMethod());
		holder.edit.setTag(item);
		holder.delete.setTag(item);
		return convertView;
	}

}

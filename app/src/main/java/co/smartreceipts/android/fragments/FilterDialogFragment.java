package co.smartreceipts.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import co.smartreceipts.android.R;
import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.filters.ReceiptAndFilter;
import co.smartreceipts.android.model.Receipt;

public class FilterDialogFragment extends DialogFragment implements OnClickListener {

	@NonNull
    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final LayoutInflater inflater = LayoutInflater.from(getActivity());
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setPositiveButton(R.string.dialog_filter_positive_button, this);
		builder.setNegativeButton(R.string.dialog_filter_negative_button, this);
		
		return builder.create();
	}
	
	
	private View buildViewsFromFilters(Filter<Receipt> filter) {
		if (filter instanceof ReceiptAndFilter) {
			
		}
		return null;
	}


	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		
	}
}

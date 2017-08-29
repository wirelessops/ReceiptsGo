package co.smartreceipts.android.settings.widget.editors;

import android.content.Context;
import android.support.v7.app.ActionBar;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.database.controllers.impl.CSVTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.ColumnTableController;
import dagger.android.support.AndroidSupportInjection;

public class CSVColumnsListFragment extends ColumnsListFragment {

	public static String TAG = "CSVColumnsListFragment";

    @Inject
    ReceiptColumnDefinitions receiptColumnDefinitions;
    @Inject
    CSVTableController csvTableController;

	public static ColumnsListFragment newInstance() {
		return new CSVColumnsListFragment();
	}

    @Override
    protected ColumnTableController getColumnTableController() {
        return csvTableController;
    }

    @Override
    protected ReceiptColumnDefinitions getReceiptColumnDefinitions() {
        return receiptColumnDefinitions;
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
            actionBar.setTitle(R.string.menu_main_csv);
        }
	}

}

package com.wops.receiptsgo.settings.widget.editors.columns;

import android.content.Context;
import androidx.appcompat.app.ActionBar;

import javax.inject.Inject;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions;
import com.wops.receiptsgo.persistence.database.controllers.TableController;
import com.wops.receiptsgo.persistence.database.controllers.impl.CSVTableController;
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderingPreferencesManager;
import com.wops.receiptsgo.workers.reports.ReportResourcesManager;
import dagger.android.support.AndroidSupportInjection;

public class CSVColumnsListFragment extends ColumnsListFragment {

    public static String TAG = "CSVColumnsListFragment";

    @Inject
    ReceiptColumnDefinitions receiptColumnDefinitions;
    @Inject
    CSVTableController csvTableController;
    @Inject
    OrderingPreferencesManager orderingPreferencesManager;
    @Inject
    ReportResourcesManager reportResourcesManager;

    public static CSVColumnsListFragment newInstance() {
        return new CSVColumnsListFragment();
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

    @Override
    protected TableController<Column<Receipt>> getTableController() {
        return csvTableController;
    }

    @Override
    protected ReceiptColumnDefinitions getReceiptColumnDefinitions() {
        return receiptColumnDefinitions;
    }

    @Override
    protected void saveTableOrdering() {
        super.saveTableOrdering();
        orderingPreferencesManager.saveCsvColumnsTableOrdering();
    }

    @Override
    protected ReportResourcesManager getReportResourcesManager() {
        return reportResourcesManager;
    }
}

package com.wops.receiptsgo.settings.widget.editors.columns;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import javax.inject.Inject;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions;
import com.wops.receiptsgo.persistence.database.controllers.TableController;
import com.wops.receiptsgo.persistence.database.controllers.impl.PDFTableController;
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderingPreferencesManager;
import com.wops.receiptsgo.workers.reports.ReportResourcesManager;
import dagger.android.support.AndroidSupportInjection;

public class PDFColumnsListFragment extends ColumnsListFragment {

    public static String TAG = "PDFColumnsListFragment";

    @Inject
    ReceiptColumnDefinitions receiptColumnDefinitions;
    @Inject
    PDFTableController pdfTableController;
    @Inject
    OrderingPreferencesManager orderingPreferencesManager;
    @Inject
    ReportResourcesManager reportResourcesManager;

    public static PDFColumnsListFragment newInstance() {
        return new PDFColumnsListFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(R.string.menu_main_pdf);
        }
    }

    @Override
    protected TableController<Column<Receipt>> getTableController() {
        return pdfTableController;
    }

    @Override
    protected ReceiptColumnDefinitions getReceiptColumnDefinitions() {
        return receiptColumnDefinitions;
    }

    @Override
    protected ReportResourcesManager getReportResourcesManager() {
        return reportResourcesManager;
    }

    @Override
    protected void saveTableOrdering() {
        super.saveTableOrdering();
        orderingPreferencesManager.savePdfColumnsTableOrdering();
    }

}

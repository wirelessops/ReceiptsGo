package co.smartreceipts.android.settings.widget.editors.columns;

import android.content.Context;
import android.support.v7.app.ActionBar;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.impl.PDFTableController;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.workers.reports.ReportResourcesManager;
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
    public void onAttach(Context context) {
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

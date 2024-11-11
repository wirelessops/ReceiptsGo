package com.wops.receiptsgo.persistence.database.controllers.impl;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.analytics.Analytics;
import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.ColumnDefinitions;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.persistence.database.controllers.alterations.StubTableActionAlterations;
import com.wops.receiptsgo.persistence.database.controllers.alterations.TableActionAlterations;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.persistence.database.tables.AbstractColumnTable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class ColumnTableController extends AbstractTableController<Column<Receipt>> {

    private final ColumnDefinitions<Receipt> mReceiptColumnDefinitions;

    ColumnTableController(@NonNull AbstractColumnTable table, @NonNull Analytics analytics, @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        this(table, new StubTableActionAlterations<Column<Receipt>>(), analytics, receiptColumnDefinitions);
    }

    private ColumnTableController(@NonNull AbstractColumnTable table, @NonNull TableActionAlterations<Column<Receipt>> tableActionAlterations,
                                 @NonNull Analytics analytics, @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        this(table, tableActionAlterations, receiptColumnDefinitions, analytics, Schedulers.io(), AndroidSchedulers.mainThread());
    }

    private ColumnTableController(@NonNull AbstractColumnTable table, @NonNull TableActionAlterations<Column<Receipt>> tableActionAlterations, @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions,
                          @NonNull Analytics analytics, @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        super(table, tableActionAlterations, analytics, subscribeOnScheduler, observeOnScheduler);
        mReceiptColumnDefinitions = Preconditions.checkNotNull(receiptColumnDefinitions);
    }

    /**
     * Inserts the default column as defined by {@link ColumnDefinitions#getDefaultInsertColumn()}
     * with customOrderId = Long.MAX_VALUE
     */
    public synchronized void insertDefaultColumn() {
        insert(mReceiptColumnDefinitions.getDefaultInsertColumn(), new DatabaseOperationMetadata());
    }
}

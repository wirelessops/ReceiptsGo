package com.wops.receiptsgo.persistence.database.controllers.impl;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import com.wops.receiptsgo.model.ColumnDefinitions;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.persistence.DatabaseHelper;

@ApplicationScope
public class CSVTableController extends ColumnTableController {

    @Inject
    public CSVTableController(DatabaseHelper databaseHelper, Analytics analytics, ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        super(databaseHelper.getCSVTable(), analytics, receiptColumnDefinitions);
    }
}

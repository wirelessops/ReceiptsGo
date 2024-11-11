package com.wops.receiptsgo.persistence.database.controllers.impl;

import javax.inject.Inject;

import com.wops.analytics.Analytics;
import com.wops.core.di.scopes.ApplicationScope;
import com.wops.receiptsgo.model.PaymentMethod;
import com.wops.receiptsgo.persistence.DatabaseHelper;
import com.wops.receiptsgo.persistence.database.controllers.alterations.PaymentMethodsTableActionAlterations;

@ApplicationScope
public class PaymentMethodsTableController extends AbstractTableController<PaymentMethod> {

    @Inject
    public PaymentMethodsTableController(DatabaseHelper databaseHelper, Analytics analytics) {
        super(databaseHelper.getPaymentMethodsTable(), new PaymentMethodsTableActionAlterations(databaseHelper.getReceiptsTable()), analytics);
    }
}

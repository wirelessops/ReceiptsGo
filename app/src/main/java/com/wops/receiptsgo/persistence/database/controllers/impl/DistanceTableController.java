package com.wops.receiptsgo.persistence.database.controllers.impl;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.persistence.DatabaseHelper;

@ApplicationScope
public class DistanceTableController extends TripForeignKeyAbstractTableController<Distance> {

    @Inject
    public DistanceTableController(DatabaseHelper databaseHelper, Analytics analytics, TripTableController tripTableController) {
        super(databaseHelper.getDistanceTable(), analytics);
        subscribe(new RefreshTripPricesListener<>(tripTableController));
    }
}

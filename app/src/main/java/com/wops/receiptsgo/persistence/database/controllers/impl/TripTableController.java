package com.wops.receiptsgo.persistence.database.controllers.impl;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.persistence.PersistenceManager;
import com.wops.receiptsgo.persistence.database.controllers.alterations.TripTableActionAlterations;

@ApplicationScope
public class TripTableController extends AbstractTableController<Trip> {

    @Inject
    public TripTableController(PersistenceManager persistenceManager, Analytics analytics) {
        super(persistenceManager.getDatabase().getTripsTable(), new TripTableActionAlterations(persistenceManager), analytics);
    }

}
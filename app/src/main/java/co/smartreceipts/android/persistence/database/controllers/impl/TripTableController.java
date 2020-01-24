package co.smartreceipts.android.persistence.database.controllers.impl;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.alterations.TripTableActionAlterations;

@ApplicationScope
public class TripTableController extends AbstractTableController<Trip> {

    @Inject
    public TripTableController(PersistenceManager persistenceManager, Analytics analytics) {
        super(persistenceManager.getDatabase().getTripsTable(), new TripTableActionAlterations(persistenceManager), analytics);
    }

}
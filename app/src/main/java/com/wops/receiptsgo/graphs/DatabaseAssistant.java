package com.wops.receiptsgo.graphs;

import javax.inject.Inject;

import com.wops.core.di.scopes.ApplicationScope;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.persistence.DatabaseHelper;
import io.reactivex.Single;


/**
 * Helper class to make presenter testing possible
 */
@ApplicationScope
public class DatabaseAssistant {

    @Inject
    DatabaseHelper databaseHelper;

    @Inject
    public DatabaseAssistant() {
    }

    public Single<Boolean> isReceiptsTableEmpty(Trip trip) {
        return databaseHelper.getReceiptsTable()
                .get(trip)
                .flatMap(receipts -> Single.just(receipts.isEmpty()));
    }
}

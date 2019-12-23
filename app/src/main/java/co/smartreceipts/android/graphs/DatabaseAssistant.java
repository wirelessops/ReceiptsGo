package co.smartreceipts.android.graphs;

import javax.inject.Inject;

import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
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

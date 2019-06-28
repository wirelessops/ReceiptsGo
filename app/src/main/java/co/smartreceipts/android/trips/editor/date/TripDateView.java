package co.smartreceipts.android.trips.editor.date;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import java.sql.Date;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * A view contract for managing the receipt date
 */
public interface TripDateView {

    /**
     * @return an Rx {@link Consumer}, which displays an end date
     */
    @NonNull
    @UiThread
    Consumer<Date> displayEndDate();

    /**
     * @return an {@link Observable} that will emit a {@link Date} any time the user changes the value for
     * the receipt date
     */
    @NonNull
    @UiThread
    Observable<Date> getStartDateChanges();

}

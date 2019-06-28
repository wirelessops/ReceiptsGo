package co.smartreceipts.android.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.Trip;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

@ApplicationScope
public class LastTripMonitor {

    private static final String PREFERENCES_FILENAME = SharedPreferenceDefinitions.LastTripTracker.toString();
    private static final String PREFERENCE_TRIP_NAME = "tripName";

    private final Context mContext;

    private Trip mTrip;

    @Inject
    public LastTripMonitor(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Retrieves the last trip that was saved via {@link #setLastTrip(Trip)}
     * from a list of known database entries
     *
     * @return the last {@link Trip} or {@code null} if none was ever saved
     */
    @Nullable
    @WorkerThread
    public synchronized Trip getLastTrip(@NonNull List<Trip> trips) {
        if (mTrip == null) {
            final SharedPreferences preferences = mContext.getSharedPreferences(PREFERENCES_FILENAME, 0);
            final String tripName = preferences.getString(PREFERENCE_TRIP_NAME, "");
            for (final Trip trip : trips) {
                if (tripName.equals(trip.getName())) {
                    mTrip = trip;
                    return mTrip;
                }
            }
        }
        return mTrip;
    }

    /**
     * Sets the last trip, which we can retrieve at a later point
     *
     * @param trip the last {@link Trip} to persist
     */
    @AnyThread
    public synchronized void setLastTrip(@NonNull Trip trip) {
        mTrip = trip;

        // Run this function in the background
        Completable.fromAction(() -> {
                    final SharedPreferences preferences = mContext.getSharedPreferences(PREFERENCES_FILENAME, 0);
                    final SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(PREFERENCE_TRIP_NAME, trip.getName());
                    editor.apply();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

}
package co.smartreceipts.android.autocomplete.di;

import co.smartreceipts.android.autocomplete.AutoCompleteInteractor;
import co.smartreceipts.android.autocomplete.distance.DistanceAutoCompleteResultsChecker;
import co.smartreceipts.android.autocomplete.distance.DistanceAutoCompletionProvider;
import co.smartreceipts.android.autocomplete.receipt.ReceiptAutoCompleteResultsChecker;
import co.smartreceipts.android.autocomplete.receipt.ReceiptAutoCompletionProvider;
import co.smartreceipts.android.autocomplete.trip.TripAutoCompleteResultsChecker;
import co.smartreceipts.android.autocomplete.trip.TripAutoCompletionProvider;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.settings.UserPreferenceManager;
import dagger.Module;
import dagger.Provides;

@Module
public class AutoCompleteModule {

    @Provides
    @ApplicationScope
    public static AutoCompleteInteractor<Trip> provideTripAutoCompletionInteractor(TripAutoCompletionProvider provider,
                                                                                   TripAutoCompleteResultsChecker resultChecker,
                                                                                   UserPreferenceManager userPreferenceManager) {
        return new AutoCompleteInteractor<>(provider, resultChecker, userPreferenceManager);
    }

    @Provides
    @ApplicationScope
    public static AutoCompleteInteractor<Receipt> provideReceiptAutoCompletionInteractor(ReceiptAutoCompletionProvider provider,
                                                                                         ReceiptAutoCompleteResultsChecker resultChecker,
                                                                                         UserPreferenceManager userPreferenceManager) {
        return new AutoCompleteInteractor<>(provider, resultChecker, userPreferenceManager);
    }

    @Provides
    @ApplicationScope
    public static AutoCompleteInteractor<Distance> provideDistanceAutoCompletionInteractor(DistanceAutoCompletionProvider provider,
                                                                                           DistanceAutoCompleteResultsChecker resultChecker,
                                                                                           UserPreferenceManager userPreferenceManager) {
        return new AutoCompleteInteractor<>(provider, resultChecker, userPreferenceManager);
    }
}

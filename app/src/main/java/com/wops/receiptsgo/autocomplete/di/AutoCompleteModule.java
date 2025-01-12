package com.wops.receiptsgo.autocomplete.di;

import com.wops.receiptsgo.autocomplete.AutoCompleteInteractor;
import com.wops.receiptsgo.autocomplete.distance.DistanceAutoCompleteResultsChecker;
import com.wops.receiptsgo.autocomplete.distance.DistanceAutoCompletionProvider;
import com.wops.receiptsgo.autocomplete.receipt.ReceiptAutoCompleteResultsChecker;
import com.wops.receiptsgo.autocomplete.receipt.ReceiptAutoCompletionProvider;
import com.wops.receiptsgo.autocomplete.trip.TripAutoCompleteResultsChecker;
import com.wops.receiptsgo.autocomplete.trip.TripAutoCompletionProvider;
import com.wops.core.di.scopes.ApplicationScope;
import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.settings.UserPreferenceManager;
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

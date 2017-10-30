package co.smartreceipts.android.persistence.database.tables.ordering;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;

@ApplicationScope
public class OrderingPreferencesManager {

    @Inject
    Context appContext;

    private static final class Keys {
        /**
         * Key to get ordering preferences
         */
        private static final String ORDERING_PREFERENCES = "Smart Receipts ordering preferences";

        /**
         * Key to track if the user has already opened "Manage categories" screen.
         * If user didn't open that screen - we need to use alphabet order
         */
        private static final String ORDERING_CATEGORIES = "categories custom ordering";
        /**
         * Key to track if the user has already opened "Manage payment methods" screen.
         * If user didn't open that screen - we need to use default order
         */
        private static final String ORDERING_PAYMENT_METHODS = "payment methods custom ordering";
    }

    @Inject
    public OrderingPreferencesManager() {
    }

    public void saveCategoriesTableOrdering() {
        getPreferencesEditor()
                .putBoolean(Keys.ORDERING_CATEGORIES, true)
                .apply();
    }

    public void savePaymentMethodsTableOrdering() {
        getPreferencesEditor()
                .putBoolean(Keys.ORDERING_PAYMENT_METHODS, true)
                .apply();
    }

    public boolean isCategoriesTableOrdered() {
        return getSharedPreferences().getBoolean(Keys.ORDERING_CATEGORIES, false);
    }

    public boolean isPaymentMethodsTableOrdered() {
        return getSharedPreferences().getBoolean(Keys.ORDERING_PAYMENT_METHODS, false);
    }

    private SharedPreferences.Editor getPreferencesEditor() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.edit();
    }

    private SharedPreferences getSharedPreferences() {
        return appContext.getSharedPreferences(Keys.ORDERING_PREFERENCES, Context.MODE_PRIVATE);
    }
}

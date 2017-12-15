package co.smartreceipts.android.persistence.database.tables.ordering;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;

@ApplicationScope
public class OrderingPreferencesManager {

    private final Context appContext;

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
        /**
         * Key to track if the user has already opened "Manage CSV columns" screen.
         * If user didn't open that screen - we need to use default order
         */
        private static final String ORDERING_COLUMNS_CSV = "csv columns custom ordering";
        /**
         * Key to track if the user has already opened "Manage PDF columns" screen.
         * If user didn't open that screen - we need to use default order
         */
        private static final String ORDERING_COLUMNS_PDF = "pdf columns custom ordering";
        /**
         * Key to track if we need to use custom_order_id column for ordering receipts.
         */
        private static final String ORDERING_RECEIPTS = "receipts custom ordering";
    }

    @Inject
    public OrderingPreferencesManager(Context context) {
        this.appContext = context;
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

    public void saveCsvColumnsTableOrdering() {
        getPreferencesEditor()
                .putBoolean(Keys.ORDERING_COLUMNS_CSV, true)
                .apply();
    }

    public void savePdfColumnsTableOrdering() {
        getPreferencesEditor()
                .putBoolean(Keys.ORDERING_COLUMNS_PDF, true)
                .apply();
    }

    public void saveReceiptsTableOrdering() {
        getPreferencesEditor()
                .putBoolean(Keys.ORDERING_RECEIPTS, true)
                .apply();
    }

    public boolean isCategoriesTableOrdered() {
        return getSharedPreferences().getBoolean(Keys.ORDERING_CATEGORIES, false);
    }

    public boolean isPaymentMethodsTableOrdered() {
        return getSharedPreferences().getBoolean(Keys.ORDERING_PAYMENT_METHODS, false);
    }

    public boolean isCsvColumnsOrdered() {
        return getSharedPreferences().getBoolean(Keys.ORDERING_COLUMNS_CSV, false);
    }

    public boolean isPdfColumnsOrdered() {
        return getSharedPreferences().getBoolean(Keys.ORDERING_COLUMNS_PDF, false);
    }

    public boolean isReceiptsTableOrdered() {
        return getSharedPreferences().getBoolean(Keys.ORDERING_RECEIPTS, false);
    }

    private SharedPreferences.Editor getPreferencesEditor() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.edit();
    }

    private SharedPreferences getSharedPreferences() {
        return appContext.getSharedPreferences(Keys.ORDERING_PREFERENCES, Context.MODE_PRIVATE);
    }
}

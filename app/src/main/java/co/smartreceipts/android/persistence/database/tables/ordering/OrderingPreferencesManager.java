package co.smartreceipts.android.persistence.database.tables.ordering;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;
import javax.inject.Named;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.persistence.database.tables.CSVTable;
import co.smartreceipts.android.persistence.database.tables.CategoriesTable;
import co.smartreceipts.android.persistence.database.tables.PDFTable;
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.persistence.database.tables.Table;
import dagger.Lazy;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

@ApplicationScope
public class OrderingPreferencesManager {

    public static final String ORDERING_PREFERENCES = "Smart Receipts ordering preferences";

    private final Lazy<SharedPreferences> sharedPreferencesLazy;

    private static final class Keys {

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
    public OrderingPreferencesManager(@NonNull @Named(ORDERING_PREFERENCES) Lazy<SharedPreferences> sharedPreferences) {
        this.sharedPreferencesLazy = Preconditions.checkNotNull(sharedPreferences);
    }

    public void initialize() {
        // Load the shared preferences in a background thread
        Completable.fromCallable(this::getSharedPreferences)
                .subscribeOn(Schedulers.io())
                .subscribe();
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

    public boolean isOrdered(Class<? extends Table<?>> tableClass) {
        if (CategoriesTable.class.equals(tableClass)) {
            return isCategoriesTableOrdered();
        } else if (PaymentMethodsTable.class.equals(tableClass)) {
            return isPaymentMethodsTableOrdered();
        } else if (CSVTable.class.equals(tableClass)) {
            return isCsvColumnsOrdered();
        } else if (PDFTable.class.equals(tableClass)) {
            return isPdfColumnsOrdered();
        } else if (ReceiptsTable.class.equals(tableClass)) {
            return isReceiptsTableOrdered();
        } else {
            return false;
        }
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
        return sharedPreferencesLazy.get();
    }
}

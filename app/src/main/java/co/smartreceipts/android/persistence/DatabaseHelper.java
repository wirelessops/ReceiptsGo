package co.smartreceipts.android.persistence;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Priceable;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.model.utils.CurrencyUtils;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.tables.CSVTable;
import co.smartreceipts.android.persistence.database.tables.CategoriesTable;
import co.smartreceipts.android.persistence.database.tables.DistanceTable;
import co.smartreceipts.android.persistence.database.tables.PDFTable;
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.persistence.database.tables.TripsTable;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.utils.sorting.AlphabeticalCaseInsensitiveCharSequenceComparator;
import io.reactivex.Single;
import wb.android.autocomplete.AutoCompleteAdapter;
import wb.android.storage.StorageManager;

@ApplicationScope
public class DatabaseHelper extends SQLiteOpenHelper implements AutoCompleteAdapter.QueryListener, AutoCompleteAdapter.ItemSelectedListener {

    // Database Info
    public static final String DATABASE_NAME = "receipts.db";
    public static final int DATABASE_VERSION = 17;

    @Deprecated
    public static final String NO_DATA = "null"; // TODO: Just set to null

    // Tags
    public static final String TAG_TRIPS_NAME = "Trips";
    public static final String TAG_TRIPS_COST_CENTER = "Trips_CostCenter";
    public static final String TAG_RECEIPTS_NAME = "Receipts";
    public static final String TAG_RECEIPTS_COMMENT = "Receipts_Comment";
    public static final String TAG_DISTANCE_LOCATION = "Distance_Location";

    // InstanceVar
    private static DatabaseHelper INSTANCE = null;


    // Caching Vars
    private ArrayList<CharSequence> mFullCurrencyList;
    private ArrayList<CharSequence> mMostRecentlyUsedCurrencyList;
    private final ReceiptColumnDefinitions mReceiptColumnDefinitions;

    // Other vars
    private final Context mContext;
    private final TableDefaultsCustomizer mCustomizations;
    private final UserPreferenceManager mPreferences;
    private final OrderingPreferencesManager mOrderingPreferencesManager;

    // Listeners
    private ReceiptAutoCompleteListener mReceiptAutoCompleteListener;

    // Locks
    private final Object mDatabaseLock = new Object();

    // Tables
    private final List<Table> mTables;
    private final TripsTable mTripsTable;
    private final ReceiptsTable mReceiptsTable;
    private final DistanceTable mDistanceTable;
    private final CategoriesTable mCategoriesTable;
    private final CSVTable mCSVTable;
    private final PDFTable mPDFTable;
    private final PaymentMethodsTable mPaymentMethodsTable;

    // Misc Vars
    private boolean mIsDBOpen = false;

    public interface ReceiptAutoCompleteListener {

        void onReceiptRowAutoCompleteQueryResult(@Nullable String name, @Nullable String price, @Nullable Integer categoryId);
    }

    public DatabaseHelper(@NonNull Context context, @NonNull StorageManager storageManager,
                          @NonNull UserPreferenceManager preferences,
                          @NonNull String databasePath, ReceiptColumnDefinitions receiptColumnDefinitions,
                          @NonNull TableDefaultsCustomizer tableDefaultsCustomizer,
                          @NonNull OrderingPreferencesManager orderingPreferencesManager) {
        super(context, databasePath, null, DATABASE_VERSION); // Requests the default cursor

        mContext = context;
        mPreferences = preferences;
        mReceiptColumnDefinitions = receiptColumnDefinitions;
        mCustomizations = tableDefaultsCustomizer;
        mOrderingPreferencesManager = orderingPreferencesManager;

        // Tables:
        mTables = new ArrayList<>();
        mTripsTable = new TripsTable(this, storageManager, preferences);
        mDistanceTable = new DistanceTable(this, mTripsTable, preferences);
        mCategoriesTable = new CategoriesTable(this, orderingPreferencesManager);
        mCSVTable = new CSVTable(this, mReceiptColumnDefinitions, orderingPreferencesManager);
        mPDFTable = new PDFTable(this, mReceiptColumnDefinitions, orderingPreferencesManager);
        mPaymentMethodsTable = new PaymentMethodsTable(this, orderingPreferencesManager);
        mReceiptsTable = new ReceiptsTable(this, mTripsTable, mPaymentMethodsTable, mCategoriesTable, storageManager, preferences, orderingPreferencesManager);
        mTables.add(mTripsTable);
        mTables.add(mDistanceTable);
        mTables.add(mCategoriesTable);
        mTables.add(mCSVTable);
        mTables.add(mPDFTable);
        mTables.add(mPaymentMethodsTable);
        mTables.add(mReceiptsTable);

        this.getReadableDatabase(); // Called here, so onCreate gets called on the UI thread
    }

    public static synchronized DatabaseHelper getInstance(Context context, StorageManager storageManager,
                                                   UserPreferenceManager preferences,
                                                   ReceiptColumnDefinitions receiptColumnDefinitions,
                                                   TableDefaultsCustomizer tableDefaultsCustomizer,
                                                   OrderingPreferencesManager orderingPreferencesManager) {
        if (INSTANCE == null || !INSTANCE.isOpen()) { // If we don't have an instance or it's closed
            String databasePath = StorageManager.GetRootPath();
            if (BuildConfig.DEBUG) {
                if (databasePath.equals("")) {
                    throw new RuntimeException("The SDCard must be created before GetRootPath is called in DBHelper");
                }
            }
            if (!databasePath.endsWith(File.separator)) {
                databasePath = databasePath + File.separator;
            }
            databasePath = databasePath + DATABASE_NAME;
            INSTANCE = new DatabaseHelper(context, storageManager, preferences, databasePath,
                    receiptColumnDefinitions, tableDefaultsCustomizer, orderingPreferencesManager);
        }
        return INSTANCE;
    }


    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // Begin Abstract Method Overrides
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(final SQLiteDatabase db) {
        Logger.info(this, "onCreate");
        Logger.info(this, "Clearing out our clear-able preferences to avoid any syncing issues due if our data was only partially wiped");
        SharedPreferenceDefinitions.clearPreferencesThatCanBeCleared(mContext);
        for (final Table table : mTables) {
            table.onCreate(db, mCustomizations);
        }

        for (final Table table : mTables) {
            table.onPostCreateUpgrade();
        }

    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, final int newVersion) {
        Logger.info(this, "onCreate from {} to {}.", oldVersion, newVersion);

        for (final Table table : mTables) {
            table.onUpgrade(db, oldVersion, newVersion, mCustomizations);
        }

        for (final Table table : mTables) {
            table.onPostCreateUpgrade();
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        mIsDBOpen = true;
    }

    @Override
    public synchronized void close() {
        super.close();
        mIsDBOpen = false;
    }

    public boolean isOpen() {
        return mIsDBOpen;
    }

    public void onDestroy() {
        try {
            this.close();
        } catch (Exception e) {
            // This can be called from finalize, so operate cautiously
            Logger.error(this, e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        onDestroy(); // Close our resources if we still need
        super.finalize();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // Utility Methods
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This class is not synchronized! Sync outside of it
     *
     * @param trip
     * @return
     */
    public void getTripPriceAndDailyPrice(final Trip trip) {
        queryTripPrice(trip);
        queryTripDailyPrice(trip);
    }

    /**
     * Queries the trips price and updates this object. This class is not synchronized! Sync outside of it
     *
     * @param trip the trip, which will be updated
     */
    private void queryTripPrice(final Trip trip) {
        final boolean onlyUseReimbursable = mPreferences.get(UserPreference.Receipts.OnlyIncludeReimbursable);
        final List<Receipt> receipts = mReceiptsTable.getBlocking(trip, true);
        final List<Priceable> prices = new ArrayList<>(receipts.size());
        for (final Receipt receipt : receipts) {
            if (!onlyUseReimbursable || receipt.isReimbursable()) {
                prices.add(receipt);
            }
        }

        if (mPreferences.get(UserPreference.Distance.IncludeDistancePriceInReports)) {
            final List<Distance> distances = mDistanceTable.getBlocking(trip, true);
            for (final Distance distance : distances) {
                prices.add(distance);
            }
        }

        trip.setPrice(new PriceBuilderFactory().setPriceables(prices, trip.getTripCurrency()).build());
    }

    /**
     * Queries the trips daily total price and updates this object. This class is not synchronized! Sync outside of it
     *
     * @param trip the trip, which will be updated
     */
    private void queryTripDailyPrice(final Trip trip) {
        final boolean onlyUseReimbursable = mPreferences.get(UserPreference.Receipts.OnlyIncludeReimbursable);
        final List<Receipt> receipts = mReceiptsTable.getBlocking(trip, true);
        final List<Priceable> prices = new ArrayList<>(receipts.size());
        for (final Receipt receipt : receipts) {
            if (!onlyUseReimbursable || receipt.isReimbursable()) {
                if (DateUtils.isToday(receipt.getDate())) {
                    prices.add(receipt);
                }
            }
        }

        if (mPreferences.get(UserPreference.Distance.IncludeDistancePriceInReports)) {
            final List<Distance> distances = mDistanceTable.getBlocking(trip, true);
            for (final Distance distance : distances) {
                if (DateUtils.isToday(distance.getDate())) {
                    prices.add(distance);
                }
            }
        }

        trip.setDailySubTotal(new PriceBuilderFactory().setPriceables(prices, trip.getTripCurrency()).build());
    }

    public Single<Integer> getNextReceiptAutoIncremenetIdHelper() {
        return Single.fromCallable(() -> {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = null;

            try {
                cursor = db.rawQuery("SELECT seq FROM SQLITE_SEQUENCE WHERE name=?", new String[]{ReceiptsTable.TABLE_NAME});
                if (cursor != null && cursor.moveToFirst() && cursor.getColumnCount() > 0) {
                    return cursor.getInt(0) + 1;
                } else {
                    return 0;
                }

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        });
    }

    public List<CharSequence> getCurrenciesList() {
        if (mFullCurrencyList != null) {
            return mFullCurrencyList;
        }
        mFullCurrencyList = new ArrayList<>();
        mFullCurrencyList.addAll(CurrencyUtils.getAllCurrencies());
        mFullCurrencyList.addAll(0, getMostRecentlyUsedCurrencies());
        return mFullCurrencyList;
    }

    private List<CharSequence> getMostRecentlyUsedCurrencies() {
        if (mMostRecentlyUsedCurrencyList != null) {
            return mMostRecentlyUsedCurrencyList;
        }
        mMostRecentlyUsedCurrencyList = new ArrayList<>();
        final String query = "SELECT " + ReceiptsTable.COLUMN_ISO4217 + ", COUNT(*) FROM " + ReceiptsTable.TABLE_NAME + " GROUP BY " + ReceiptsTable.COLUMN_ISO4217;
        synchronized (mDatabaseLock) {
            Cursor cursor = null;
            try {
                final SQLiteDatabase db = this.getReadableDatabase();
                cursor = db.rawQuery(query, new String[0]);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        mMostRecentlyUsedCurrencyList.add(cursor.getString(0));
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        Collections.sort(mMostRecentlyUsedCurrencyList, new AlphabeticalCaseInsensitiveCharSequenceComparator());
        return mMostRecentlyUsedCurrencyList;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // Tables Methods
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    @NonNull
    public final TripsTable getTripsTable() {
        return mTripsTable;
    }

    @NonNull
    public final ReceiptsTable getReceiptsTable() {
        return mReceiptsTable;
    }

    @NonNull
    public final DistanceTable getDistanceTable() {
        return mDistanceTable;
    }

    @NonNull
    public final CategoriesTable getCategoriesTable() {
        return mCategoriesTable;
    }

    @NonNull
    public final CSVTable getCSVTable() {
        return mCSVTable;
    }

    @NonNull
    public final PDFTable getPDFTable() {
        return mPDFTable;
    }

    @NonNull
    public final PaymentMethodsTable getPaymentMethodsTable() {
        return mPaymentMethodsTable;
    }

    @NonNull
    public final List<Table> getTables() {
        return mTables;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // AutoCompleteTextView Methods
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    public void registerReceiptAutoCompleteListener(ReceiptAutoCompleteListener listener) {
        mReceiptAutoCompleteListener = listener;
    }

    public void unregisterReceiptAutoCompleteListener() {
        mReceiptAutoCompleteListener = null;
    }

    @Override
    public Cursor getAutoCompleteCursor(CharSequence text, CharSequence tag) {
        // TODO: Fix SQL vulnerabilities
        final SQLiteDatabase db = this.getReadableDatabase();
        String sqlQuery = "";
        if (tag == TAG_RECEIPTS_NAME) {
            sqlQuery = " SELECT DISTINCT TRIM(" + ReceiptsTable.COLUMN_NAME + ") AS _id " + " FROM " + ReceiptsTable.TABLE_NAME + " WHERE " + ReceiptsTable.COLUMN_NAME + " LIKE '%" + text + "%' " + " ORDER BY " + ReceiptsTable.COLUMN_NAME;
        } else if (tag == TAG_RECEIPTS_COMMENT) {
            sqlQuery = " SELECT DISTINCT TRIM(" + ReceiptsTable.COLUMN_COMMENT + ") AS _id " + " FROM " + ReceiptsTable.TABLE_NAME + " WHERE " + ReceiptsTable.COLUMN_COMMENT + " LIKE '%" + text + "%' " + " ORDER BY " + ReceiptsTable.COLUMN_COMMENT;
        } else if (tag == TAG_TRIPS_NAME) {
            sqlQuery = " SELECT DISTINCT TRIM(" + TripsTable.COLUMN_NAME + ") AS _id " + " FROM " + TripsTable.TABLE_NAME + " WHERE " + TripsTable.COLUMN_NAME + " LIKE '%" + text + "%' " + " ORDER BY " + TripsTable.COLUMN_NAME;
        } else if (tag == TAG_TRIPS_COST_CENTER) {
            sqlQuery = " SELECT DISTINCT TRIM(" + TripsTable.COLUMN_COST_CENTER + ") AS _id " + " FROM " + TripsTable.TABLE_NAME + " WHERE " + TripsTable.COLUMN_COST_CENTER + " LIKE '%" + text + "%' " + " ORDER BY " + TripsTable.COLUMN_COST_CENTER;
        } else if (tag == TAG_DISTANCE_LOCATION) {
            sqlQuery = " SELECT DISTINCT TRIM(" + DistanceTable.COLUMN_LOCATION + ") AS _id " + " FROM " + DistanceTable.TABLE_NAME + " WHERE " + DistanceTable.COLUMN_LOCATION + " LIKE '%" + text + "%' " + " ORDER BY " + DistanceTable.COLUMN_LOCATION;
        }
        synchronized (mDatabaseLock) {
            return db.rawQuery(sqlQuery, null);
        }
    }

    @Override
    public void onItemSelected(CharSequence text, CharSequence tag) {
        // TODO: Make Async

        Cursor c = null;
        SQLiteDatabase db = null;
        final String name = text.toString();
        if (tag == TAG_RECEIPTS_NAME) {
            Integer categoryId = null;
            String price = null;
            // If we're not predicting, return
            if (!mPreferences.get(UserPreference.Receipts.PredictCategories)) {
                // price = null;
                // category = null
            } else {
                synchronized (mDatabaseLock) {
                    try {
                        db = this.getReadableDatabase();
                        c = db.query(ReceiptsTable.TABLE_NAME,
                                new String[]{ReceiptsTable.COLUMN_CATEGORY_ID, ReceiptsTable.COLUMN_PRICE},
                                ReceiptsTable.COLUMN_NAME + "= ?",
                                new String[]{name}, null, null, ReceiptsTable.COLUMN_DATE + " DESC", "2");
                        if (c != null && c.getCount() == 2) {
                            if (c.moveToFirst()) {
                                categoryId = c.getInt(0);
                                price = c.getString(1);
                                if (c.moveToNext()) {
                                    if (!categoryId.equals(c.getInt(0))) {
                                        categoryId = null;
                                    }
                                    if (!price.equalsIgnoreCase(c.getString(1))) {
                                        price = null;
                                    }
                                }
                            }
                        }
                    } finally {
                        if (c != null) {
                            c.close();
                        }
                    }
                }
            }
            if (mReceiptAutoCompleteListener != null) {
                mReceiptAutoCompleteListener.onReceiptRowAutoCompleteQueryResult(name, price, categoryId);
            }
        }
    }

}

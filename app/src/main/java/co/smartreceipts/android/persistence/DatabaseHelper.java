package co.smartreceipts.android.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

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
import co.smartreceipts.android.persistence.database.defaults.WhiteLabelFriendlyTableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.tables.AbstractSqlTable;
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
    public static final int DATABASE_VERSION = 16;

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
                          WhiteLabelFriendlyTableDefaultsCustomizer tableDefaultsCustomizer,
                          @NonNull OrderingPreferencesManager orderingPreferencesManager) {
        super(context, databasePath, null, DATABASE_VERSION); // Requests the default cursor

        mContext = context;
        mPreferences = preferences;
        mReceiptColumnDefinitions = receiptColumnDefinitions;
        mCustomizations = tableDefaultsCustomizer;

        // Tables:
        mTables = new ArrayList<>();
        mTripsTable = new TripsTable(this, storageManager, preferences);
        mDistanceTable = new DistanceTable(this, mTripsTable, preferences.get(UserPreference.General.DefaultCurrency));
        mCategoriesTable = new CategoriesTable(this, orderingPreferencesManager.isCategoriesTableOrdered());
        mCSVTable = new CSVTable(this, mReceiptColumnDefinitions, orderingPreferencesManager.isCsvColumnsOrdered());
        mPDFTable = new PDFTable(this, mReceiptColumnDefinitions, orderingPreferencesManager.isPdfColumnsOrdered());
        mPaymentMethodsTable = new PaymentMethodsTable(this, orderingPreferencesManager.isPaymentMethodsTableOrdered());
        mReceiptsTable = new ReceiptsTable(this, mTripsTable, mPaymentMethodsTable, mCategoriesTable,
                storageManager, preferences, orderingPreferencesManager.isReceiptsTableOrdered());
        mTables.add(mTripsTable);
        mTables.add(mDistanceTable);
        mTables.add(mCategoriesTable);
        mTables.add(mCSVTable);
        mTables.add(mPDFTable);
        mTables.add(mPaymentMethodsTable);
        mTables.add(mReceiptsTable);

        this.getReadableDatabase(); // Called here, so onCreate gets called on the UI thread
    }

    public static final DatabaseHelper getInstance(Context context, StorageManager storageManager,
                                                   UserPreferenceManager preferences,
                                                   ReceiptColumnDefinitions receiptColumnDefinitions,
                                                   WhiteLabelFriendlyTableDefaultsCustomizer tableDefaultsCustomizer,
                                                   OrderingPreferencesManager orderingPreferencesManager) {
        if (INSTANCE == null || !INSTANCE.isOpen()) { // If we don't have an instance or it's closed
            String databasePath = StorageManager.GetRootPath();
            if (BuildConfig.DEBUG) {
                if (databasePath.equals("")) {
                    throw new RuntimeException("The SDCard must be created beforoe GetRootPath is called in DBHelper");
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
    public final void onUpgrade(final SQLiteDatabase db, int oldVersion, final int newVersion) {
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
    // Merge
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    public final synchronized boolean merge(String dbPath, String packageName, boolean overwrite) {
        synchronized (mDatabaseLock) {
            SQLiteDatabase importDB = null, currDB = null;
            Cursor c = null, countCursor = null;

            try {
                if (dbPath == null) {
                    Logger.debug(this, "Null database file");
                    return false;
                }
                currDB = this.getWritableDatabase();
                importDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);

                // updating backup db to current version
                final int oldVersion = importDB.getVersion();
                if (oldVersion < DATABASE_VERSION) {
                    importDB.beginTransaction();
                    try {
                        onUpgrade(importDB, oldVersion, DATABASE_VERSION);

                        importDB.setVersion(DATABASE_VERSION);
                        importDB.setTransactionSuccessful();
                    } finally {
                        importDB.endTransaction();
                    }
                }

                // attaching backup db to allow queries which can use tables from both current and backup tables together
                currDB.execSQL("ATTACH ? AS backup_db", new String[]{dbPath});


                // Merge Trips
                try {
                    Logger.debug(this, "Merging Trips");

                    c = importDB.query(TripsTable.TABLE_NAME, null, null, null, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        final int nameIndex = c.getColumnIndex(TripsTable.COLUMN_NAME);
                        final int fromIndex = c.getColumnIndex(TripsTable.COLUMN_FROM);
                        final int fromTimeZoneIndex = c.getColumnIndex(TripsTable.COLUMN_FROM_TIMEZONE);
                        final int toIndex = c.getColumnIndex(TripsTable.COLUMN_TO);
                        final int toTimeZoneIndex = c.getColumnIndex(TripsTable.COLUMN_TO_TIMEZONE);
                        final int commentIndex = c.getColumnIndex(TripsTable.COLUMN_COMMENT);
                        final int filtersIndex = c.getColumnIndex(TripsTable.COLUMN_FILTERS);
                        final int costCenterIndex = c.getColumnIndex(TripsTable.COLUMN_COST_CENTER);
                        final int processingStatusIndex = c.getColumnIndex(TripsTable.COLUMN_PROCESSING_STATUS);
                        final int defaultCurrencyIndex = c.getColumnIndex(TripsTable.COLUMN_DEFAULT_CURRENCY);
                        final int driveMarkedForDeletionIndex = c.getColumnIndex(AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION);
                        do {
                            final boolean driveMarkedForDeletion = getBoolean(c, driveMarkedForDeletionIndex, false);
                            if (!driveMarkedForDeletion) {
                                String name = getString(c, nameIndex, "");
                                if (name.contains("wb.receipts")) { // Backwards compatibility stuff
                                    if (packageName.equalsIgnoreCase("wb.receipts")) {
                                        name = name.replace("wb.receiptspro/", "wb.receipts/");
                                    } else if (packageName.equalsIgnoreCase("wb.receiptspro")) {
                                        name = name.replace("wb.receipts/", "wb.receiptspro/");
                                    }
                                    File f = new File(name);
                                    name = f.getName();
                                }
                                final long from = getLong(c, fromIndex, 0L);
                                final long to = getLong(c, toIndex, 0L);
                                final String comment = getString(c, commentIndex, "");
                                final String filters = getString(c, filtersIndex, "");
                                final String costCenter = getString(c, costCenterIndex, "");
                                final String processingStatus = getString(c, processingStatusIndex, "");
                                final String defaultCurrency = getString(c, defaultCurrencyIndex, mPreferences.get(UserPreference.General.DefaultCurrency));
                                ContentValues values = new ContentValues(10);
                                values.put(TripsTable.COLUMN_NAME, name);
                                values.put(TripsTable.COLUMN_FROM, from);
                                values.put(TripsTable.COLUMN_TO, to);
                                values.put(TripsTable.COLUMN_COMMENT, comment);
                                values.put(TripsTable.COLUMN_FILTERS, filters);
                                values.put(TripsTable.COLUMN_COST_CENTER, costCenter);
                                values.put(TripsTable.COLUMN_PROCESSING_STATUS, processingStatus);
                                values.put(TripsTable.COLUMN_DEFAULT_CURRENCY, defaultCurrency);
                                values.put(AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME, System.currentTimeMillis());
                                if (fromTimeZoneIndex > 0) {
                                    final String fromTimeZone = c.getString(fromTimeZoneIndex);
                                    values.put(TripsTable.COLUMN_FROM_TIMEZONE, fromTimeZone);
                                }
                                if (toTimeZoneIndex > 0) {
                                    final String toTimeZome = c.getString(toTimeZoneIndex);
                                    values.put(TripsTable.COLUMN_TO_TIMEZONE, toTimeZome);
                                }
                                if (overwrite) {
                                    currDB.insertWithOnConflict(TripsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                } else {
                                    currDB.insertWithOnConflict(TripsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                                }
                            }
                        }
                        while (c.moveToNext());
                    }
                } catch (SQLiteException e) {
                    Logger.error(this, "Caught sql exception during import at [a1]", e); // Occurs if Table does not exist
                } finally {
                    if (c != null && !c.isClosed()) {
                        c.close();
                        c = null;
                    }
                }

                // Merge Categories
                Logger.debug(this, "Merging Categories");

                // getting unique categories from old db which are not present in the new db and inserting
                final String mergeCategories = "INSERT INTO " + CategoriesTable.TABLE_NAME +
                        "( " + CategoriesTable.COLUMN_NAME + ", " + CategoriesTable.COLUMN_CODE + ", " + CategoriesTable.COLUMN_BREAKDOWN + ", " + AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME + ", " + AbstractSqlTable.COLUMN_CUSTOM_ORDER_ID + /* ", " + COLUMN_OLD_ID + */")" +
                        " SELECT backup_categories." + CategoriesTable.COLUMN_NAME + ", backup_categories." + CategoriesTable.COLUMN_CODE + ", backup_categories." + CategoriesTable.COLUMN_BREAKDOWN + ", \"" + System.currentTimeMillis() + "\"" + ", \"" + Integer.MAX_VALUE + "\"" + // ", backup_categories." + CategoriesTable.COLUMN_ID  +
                        " FROM backup_db." + CategoriesTable.TABLE_NAME + " backup_categories " +
                        " LEFT JOIN " + CategoriesTable.TABLE_NAME + " current_categories " +
                        " ON backup_categories." + CategoriesTable.COLUMN_NAME + " = current_categories." + CategoriesTable.COLUMN_NAME +
                        " WHERE current_categories." + CategoriesTable.COLUMN_NAME + " IS NULL" +
                        " AND backup_categories." + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " = 0;";
                Logger.debug(this, mergeCategories);
                currDB.execSQL(mergeCategories);


                // Merge CSV
                // Replacing current table with imported one
                Logger.debug(this, "Merging CSV");

                currDB.execSQL("DROP TABLE " + CSVTable.TABLE_NAME);
                currDB.execSQL("CREATE TABLE " + CSVTable.TABLE_NAME + " AS SELECT * FROM backup_db." + CSVTable.TABLE_NAME);


                // Merge PDF
                // Replacing current table with imported one
                Logger.debug(this, "Merging PDF");

                currDB.execSQL("DROP TABLE " + PDFTable.TABLE_NAME);
                currDB.execSQL("CREATE TABLE " + PDFTable.TABLE_NAME + " AS SELECT * FROM backup_db." + PDFTable.TABLE_NAME);


                // Merge Payment methods
                Logger.debug(this, "Merging Payment Methods");

                final String mergePaymentMethods = "INSERT INTO " + PaymentMethodsTable.TABLE_NAME +
                        "( " + PaymentMethodsTable.COLUMN_METHOD + ", " + AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME + ", " + AbstractSqlTable.COLUMN_CUSTOM_ORDER_ID + ")" +
                        " SELECT backup_methods." + PaymentMethodsTable.COLUMN_METHOD + ", \"" + System.currentTimeMillis() + "\"" + ", \"" + Integer.MAX_VALUE + "\"" +
                        " FROM backup_db." + PaymentMethodsTable.TABLE_NAME + " backup_methods " +
                        " LEFT JOIN " + PaymentMethodsTable.TABLE_NAME + " current_methods " +
                        " ON backup_methods." + PaymentMethodsTable.COLUMN_METHOD + " = current_methods." + PaymentMethodsTable.COLUMN_METHOD +
                        " WHERE current_methods." + PaymentMethodsTable.COLUMN_METHOD + " IS NULL" +
                        " AND backup_methods." + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " = 0;";
                Logger.debug(this, mergePaymentMethods);
                currDB.execSQL(mergePaymentMethods);


                // Merge Distances
                Logger.debug(this, "Merging Distance");
                try {
                    c = importDB.query(DistanceTable.TABLE_NAME, null, null, null, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        final String distanceCountQuery = "SELECT COUNT(*), " + DistanceTable.COLUMN_ID + " FROM " + DistanceTable.TABLE_NAME + " WHERE " + DistanceTable.COLUMN_PARENT + "=? AND " + DistanceTable.COLUMN_LOCATION + "=? AND " + DistanceTable.COLUMN_DATE + "=?";
                        final int parentTripIndex = c.getColumnIndex(DistanceTable.COLUMN_PARENT);
                        final int locationIndex = c.getColumnIndex(DistanceTable.COLUMN_LOCATION);
                        final int distanceIndex = c.getColumnIndex(DistanceTable.COLUMN_DISTANCE);
                        final int rateIndex = c.getColumnIndex(DistanceTable.COLUMN_RATE);
                        final int currencyIndex = c.getColumnIndex(DistanceTable.COLUMN_RATE_CURRENCY);
                        final int dateIndex = c.getColumnIndex(DistanceTable.COLUMN_DATE);
                        final int timezoneIndex = c.getColumnIndex(DistanceTable.COLUMN_TIMEZONE);
                        final int commentIndex = c.getColumnIndex(DistanceTable.COLUMN_COMMENT);
                        final int driveMarkedForDeletionIndex = c.getColumnIndex(AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION);
                        do {
                            final boolean driveMarkedForDeletion = getBoolean(c, driveMarkedForDeletionIndex, false);
                            if (!driveMarkedForDeletion) {
                                final ContentValues values = new ContentValues(8);
                                final String parentTripPath = getString(c, parentTripIndex, "");
                                final String location = getString(c, locationIndex, "");
                                final BigDecimal distance = getDecimal(c, distanceIndex);
                                final BigDecimal rate = getDecimal(c, rateIndex);
                                final String currency = getString(c, currencyIndex, mPreferences.get(UserPreference.General.DefaultCurrency));
                                final long date = getLong(c, dateIndex, 0L);
                                final String timezone = getString(c, timezoneIndex, TimeZone.getDefault().getID());
                                final String comment = getString(c, commentIndex, "");
                                values.put(DistanceTable.COLUMN_PARENT, parentTripPath);
                                values.put(DistanceTable.COLUMN_LOCATION, location);
                                values.put(DistanceTable.COLUMN_DISTANCE, distance.doubleValue());
                                values.put(DistanceTable.COLUMN_RATE, rate.doubleValue());
                                values.put(DistanceTable.COLUMN_RATE_CURRENCY, currency);
                                values.put(DistanceTable.COLUMN_DATE, date);
                                values.put(DistanceTable.COLUMN_TIMEZONE, timezone);
                                values.put(DistanceTable.COLUMN_COMMENT, comment);
                                values.put(AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME, System.currentTimeMillis());
                                try {
                                    countCursor = currDB.rawQuery(distanceCountQuery, new String[]{parentTripPath, location, Long.toString(date)});
                                    if (countCursor != null && countCursor.moveToFirst()) {
                                        int count = countCursor.getInt(0);
                                        int updateID = countCursor.getInt(1);
                                        if (count > 0 && overwrite) { // Update
                                            currDB.update(DistanceTable.TABLE_NAME, values, DistanceTable.COLUMN_ID + " = ?", new String[]{Integer.toString(updateID)});
                                        } else { // insert
                                            if (overwrite) {
                                                currDB.insertWithOnConflict(DistanceTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                            } else if (count == 0) {
                                                // If we're not overwriting anything, let's check that there are no entries here
                                                currDB.insertWithOnConflict(DistanceTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                                            }
                                        }
                                    }
                                } finally {
                                    if (countCursor != null && !countCursor.isClosed()) {
                                        countCursor.close();
                                        countCursor = null;
                                    }
                                }
                            }
                        }
                        while (c.moveToNext());
                    }
                } catch (SQLiteException e) {
                    Logger.error(this, "Caught sql exception during import at [a6]", e); // Occurs if Table does not exist
                } finally {
                    if (c != null && !c.isClosed()) {
                        c.close();
                        c = null;
                    }
                }

                // Merge Receipts
                // Note: Receipts table must be the last merged one
                Logger.debug(this, "Merging Receipts");

                // replacing old category id's with new ones in the backup receipts table
                final String updateOldCategoriesIds = "UPDATE backup_db." + ReceiptsTable.TABLE_NAME +
                        " SET " + ReceiptsTable.COLUMN_CATEGORY_ID +
                        " = ( SELECT curr_c." + CategoriesTable.COLUMN_ID +
                        " FROM " + CategoriesTable.TABLE_NAME + " curr_c, backup_db." + CategoriesTable.TABLE_NAME + " backup_c " +
                        " WHERE backup_db." + ReceiptsTable.TABLE_NAME + "." + ReceiptsTable.COLUMN_CATEGORY_ID + " = backup_c." + CategoriesTable.COLUMN_ID +
                        " AND backup_c." + CategoriesTable.COLUMN_NAME + " = curr_c." + CategoriesTable.COLUMN_NAME +
                        " AND backup_c." + CategoriesTable.COLUMN_CODE + " = curr_c." + CategoriesTable.COLUMN_CODE +
                        " LIMIT 1);";
                Logger.debug(this, updateOldCategoriesIds);
                currDB.execSQL(updateOldCategoriesIds);

                // replacing old payment method id's with new ones in the backup receipts table
                final String updateOldMethodsIds = "UPDATE backup_db." + ReceiptsTable.TABLE_NAME +
                        " SET " + ReceiptsTable.COLUMN_PAYMENT_METHOD_ID +
                        " = ( SELECT curr_pm." + PaymentMethodsTable.COLUMN_ID +
                        " FROM " + PaymentMethodsTable.TABLE_NAME + " curr_pm, backup_db." + PaymentMethodsTable.TABLE_NAME + " backup_pm " +
                        " WHERE backup_db." + ReceiptsTable.TABLE_NAME + "." + ReceiptsTable.COLUMN_PAYMENT_METHOD_ID + " = backup_pm." + PaymentMethodsTable.COLUMN_ID +
                        " AND backup_pm." + PaymentMethodsTable.COLUMN_METHOD + " = curr_pm." + PaymentMethodsTable.COLUMN_METHOD +
                        " LIMIT 1);";
                Logger.debug(this, updateOldMethodsIds);
                currDB.execSQL(updateOldMethodsIds);


                try {
                    final String queryCount = "SELECT COUNT(*), " + ReceiptsTable.COLUMN_ID + " FROM " + ReceiptsTable.TABLE_NAME + " WHERE " + ReceiptsTable.COLUMN_PATH + "=? AND " + ReceiptsTable.COLUMN_NAME + "=? AND " + ReceiptsTable.COLUMN_DATE + "=?";
                    c = importDB.query(ReceiptsTable.TABLE_NAME, null, null, null, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        final int pathIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PATH);
                        final int nameIndex = c.getColumnIndex(ReceiptsTable.COLUMN_NAME);
                        final int parentIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PARENT);
                        final int categoryIdIndex = c.getColumnIndex(ReceiptsTable.COLUMN_CATEGORY_ID);
                        final int priceIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PRICE);
                        final int dateIndex = c.getColumnIndex(ReceiptsTable.COLUMN_DATE);
                        final int commentIndex = c.getColumnIndex(ReceiptsTable.COLUMN_COMMENT);
                        final int reimbursableIndex = c.getColumnIndex(ReceiptsTable.COLUMN_REIMBURSABLE);
                        final int currencyIndex = c.getColumnIndex(ReceiptsTable.COLUMN_ISO4217);
                        final int fullpageIndex = c.getColumnIndex(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE);
                        final int extra_edittext_1_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1);
                        final int extra_edittext_2_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2);
                        final int extra_edittext_3_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3);
                        final int taxIndex = c.getColumnIndex(ReceiptsTable.COLUMN_TAX);
                        final int timeZoneIndex = c.getColumnIndex(ReceiptsTable.COLUMN_TIMEZONE);
                        final int paymentMethodIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID);
                        final int processingStatusIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PROCESSING_STATUS);
                        final int exchangeRateIndex = c.getColumnIndex(ReceiptsTable.COLUMN_EXCHANGE_RATE);
                        final int driveMarkedForDeletionIndex = c.getColumnIndex(AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION);
                        do {
                            final boolean driveMarkedForDeletion = getBoolean(c, driveMarkedForDeletionIndex, false);
                            if (!driveMarkedForDeletion) {
                                final String oldPath = getString(c, pathIndex, "");
                                String newPath = oldPath != null ? oldPath : "";
                                if (newPath.contains("wb.receipts")) { // Backwards compatibility stuff
                                    if (packageName.equalsIgnoreCase("wb.receipts")) {
                                        newPath = oldPath.replace("wb.receiptspro/", "wb.receipts/");
                                    } else if (packageName.equalsIgnoreCase("wb.receiptspro")) {
                                        newPath = oldPath.replace("wb.receipts/", "wb.receiptspro/");
                                    }
                                    File f = new File(newPath);
                                    newPath = f.getName();
                                }
                                final String name = getString(c, nameIndex, "");
                                final String oldParent = getString(c, parentIndex, "");
                                String newParent = oldParent != null ? oldParent : "";
                                if (newParent.contains("wb.receipts")) { // Backwards compatibility stuff
                                    if (packageName.equalsIgnoreCase("wb.receipts")) {
                                        newParent = oldParent.replace("wb.receiptspro/", "wb.receipts/");
                                    } else if (packageName.equalsIgnoreCase("wb.receiptspro")) {
                                        newParent = oldParent.replace("wb.receipts/", "wb.receiptspro/");
                                    }
                                    File f = new File(newParent);
                                    newParent = f.getName();
                                }
                                final int categoryId = getInt(c, categoryIdIndex, 0);
                                final BigDecimal price = getDecimal(c, priceIndex);
                                final long date = getLong(c, dateIndex, 0L);
                                final String comment = getString(c, commentIndex, "");
                                final boolean reimbursable = getBoolean(c, reimbursableIndex, true);
                                final String currency = getString(c, currencyIndex, mPreferences.get(UserPreference.General.DefaultCurrency));
                                final boolean fullpage = getBoolean(c, fullpageIndex, false);
                                final String extra_edittext_1 = getString(c, extra_edittext_1_Index, null);
                                final String extra_edittext_2 = getString(c, extra_edittext_2_Index, null);
                                final String extra_edittext_3 = getString(c, extra_edittext_3_Index, null);
                                final BigDecimal tax = getDecimal(c, taxIndex);
                                final int paymentMethod = getInt(c, paymentMethodIndex, 0);
                                final String processingStatus = getString(c, processingStatusIndex, "");
                                final BigDecimal exchangeRate = getDecimal(c, exchangeRateIndex);
                                try {
                                    countCursor = currDB.rawQuery(queryCount, new String[]{newPath, name, Long.toString(date)});
                                    if (countCursor != null && countCursor.moveToFirst()) {
                                        int count = countCursor.getInt(0);
                                        int updateID = countCursor.getInt(1);
                                        final ContentValues values = new ContentValues(14);
                                        values.put(ReceiptsTable.COLUMN_PATH, newPath);
                                        values.put(ReceiptsTable.COLUMN_NAME, name);
                                        values.put(ReceiptsTable.COLUMN_PARENT, newParent);
                                        values.put(ReceiptsTable.COLUMN_CATEGORY_ID, categoryId);
                                        values.put(ReceiptsTable.COLUMN_PRICE, price.doubleValue());
                                        values.put(ReceiptsTable.COLUMN_DATE, date);
                                        values.put(ReceiptsTable.COLUMN_COMMENT, comment);
                                        values.put(ReceiptsTable.COLUMN_REIMBURSABLE, reimbursable);
                                        values.put(ReceiptsTable.COLUMN_ISO4217, currency);
                                        values.put(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE, fullpage);
                                        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1, extra_edittext_1);
                                        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2, extra_edittext_2);
                                        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3, extra_edittext_3);
                                        values.put(ReceiptsTable.COLUMN_TAX, tax.doubleValue());
                                        values.put(ReceiptsTable.COLUMN_PROCESSING_STATUS, processingStatus);
                                        values.put(ReceiptsTable.COLUMN_EXCHANGE_RATE, exchangeRate.doubleValue());
                                        values.put(AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME, System.currentTimeMillis());
                                        if (timeZoneIndex > 0) {
                                            final String timeZone = c.getString(timeZoneIndex);
                                            values.put(ReceiptsTable.COLUMN_TIMEZONE, timeZone);
                                        }
                                        values.put(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID, paymentMethod);
                                        if (count > 0 && overwrite) { // Update
                                            currDB.update(ReceiptsTable.TABLE_NAME, values, ReceiptsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(updateID)});
                                        } else { // insert
                                            if (overwrite) {
                                                currDB.insertWithOnConflict(ReceiptsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                            } else if (count == 0) {
                                                // If we're not overwriting anything, let's check that there are no entries here
                                                currDB.insertWithOnConflict(ReceiptsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                                            }
                                        }
                                    }
                                } finally {
                                    if (countCursor != null && !countCursor.isClosed()) {
                                        countCursor.close();
                                        countCursor = null;
                                    }
                                }
                            }
                        }
                        while (c.moveToNext());
                    }
                } catch (SQLiteException e) {
                    Logger.error(this, "Caught sql exception during import at [a2]", e); // Occurs if Table does not exist
                } finally {
                    if (c != null && !c.isClosed()) {
                        c.close();
                        c = null;
                    }
                }

                Logger.debug(this, "Success");
                return true;
            } catch (Exception e) {
                Logger.error(this, "Caught fatal db exception during import at [a7]:", e);
                return false;
            } finally {

                currDB.execSQL("DETACH backup_db");

                if (c != null && !c.isClosed()) {
                    c.close();
                }
                if (countCursor != null && !countCursor.isClosed()) {
                    countCursor.close();
                }
                if (importDB != null) {
                    importDB.close();
                }
            }
        }
    }

    private boolean getBoolean(Cursor cursor, int index, boolean defaultValue) {
        if (index >= 0) {
            return (cursor.getInt(index) > 0);
        } else {
            return defaultValue;
        }
    }

    private int getInt(Cursor cursor, int index, int defaultValue) {
        if (index >= 0) {
            return cursor.getInt(index);
        } else {
            return defaultValue;
        }
    }

    private long getLong(Cursor cursor, int index, long defaultValue) {
        if (index >= 0) {
            return cursor.getLong(index);
        } else {
            return defaultValue;
        }
    }

    private double getDouble(Cursor cursor, int index, double defaultValue) {
        if (index >= 0) {
            return cursor.getDouble(index);
        } else {
            return defaultValue;
        }
    }

    private String getString(Cursor cursor, int index, String defaultValue) {
        if (index >= 0) {
            return cursor.getString(index);
        } else {
            return defaultValue;
        }
    }

    /**
     * Please note that a very frustrating bug exists here. Android cursors only return the first 6
     * characters of a price string if that string contains a '.' character. It returns all of them
     * if not. This means we'll break for prices over 5 digits unless we are using a comma separator,
     * which we'd do in the EU. In the EU (comma separated), Android returns the wrong value when we
     * get a double (instead of a string). This method has been built to handle this edge case to the
     * best of our abilities.
     * <p/>
     * TODO: Longer term, everything should be saved with a decimal point
     *
     * @param cursor - the current {@link Cursor}
     * @param index  - the index of the column
     * @return a {@link BigDecimal} value of the decimal
     * @see "https://code.google.com/p/android/issues/detail?id=22219."
     */
    private BigDecimal getDecimal(@NonNull Cursor cursor, int index) {
        return getDecimal(cursor, index, new BigDecimal(0));
    }

    private BigDecimal getDecimal(@NonNull Cursor cursor, int index, @Nullable BigDecimal defaultValue) {
        if (index >= 0) {
            final String decimalString = cursor.getString(index);
            final double decimalDouble = cursor.getDouble(index);
            if (!TextUtils.isEmpty(decimalString) && decimalString.contains(",")) {
                try {
                    return new BigDecimal(decimalString.replace(",", "."));
                } catch (NumberFormatException e) {
                    return new BigDecimal(decimalDouble);
                }
            } else {
                return new BigDecimal(decimalDouble);
            }
        } else {
            return defaultValue;
        }
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

package com.wops.receiptsgo.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.model.PaymentMethod;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.persistence.database.defaults.TableDefaultsCustomizer;
import com.wops.receiptsgo.persistence.database.tables.adapters.DistanceDatabaseAdapter;
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderByColumn;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;

/**
 * Stores all database operations related to the {@link Distance} model objects
 */
public class DistanceTable extends TripForeignKeyAbstractSqlTable<Distance> {

    // SQL Definitions:
    public static final String TABLE_NAME = "distance";

    public static final String COLUMN_PARENT_TRIP_ID = "parentKey";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIMEZONE = "timezone";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_RATE = "rate";
    public static final String COLUMN_RATE_CURRENCY = "rate_currency";
    public static final String COLUMN_PAYMENT_METHOD_ID = "paymentMethodKey";
    public static final String COLUMN_LOCATION_HIDDEN_AUTO_COMPLETE = "location_hidden_auto_complete";
    public static final String COLUMN_COMMENT_HIDDEN_AUTO_COMPLETE = "comment_hidden_auto_complete";

    @Deprecated
    public static final String COLUMN_PARENT = "parent";

    private final UserPreferenceManager userPreferenceManager;

    public DistanceTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull Table<Trip> tripsTable,
                         @NonNull Table<PaymentMethod> paymentMethodTable, @NonNull UserPreferenceManager userPreferenceManager) {
        super(sqLiteOpenHelper, TABLE_NAME, new DistanceDatabaseAdapter(tripsTable, paymentMethodTable), COLUMN_PARENT_TRIP_ID,
                new OrderByColumn(COLUMN_DATE, true));
        this.userPreferenceManager = Preconditions.checkNotNull(userPreferenceManager);
    }

    @Override
    public synchronized void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        super.onCreate(db, customizer);
        final String sql = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PARENT_TRIP_ID + " INTEGER REFERENCES " + TripsTable.COLUMN_NAME + " ON DELETE CASCADE,"
                + COLUMN_DISTANCE + " DECIMAL(10, 2) DEFAULT 0.00,"
                + COLUMN_LOCATION + " TEXT,"
                + COLUMN_DATE + " DATE,"
                + COLUMN_TIMEZONE + " TEXT,"
                + COLUMN_COMMENT + " TEXT,"
                + COLUMN_RATE_CURRENCY + " TEXT NOT NULL, "
                + COLUMN_RATE + " DECIMAL(10, 2) DEFAULT 0.00, "
                + COLUMN_PAYMENT_METHOD_ID + " INTEGER REFERENCES " + PaymentMethodsTable.TABLE_NAME + " ON DELETE NO ACTION, "
                + COLUMN_LOCATION_HIDDEN_AUTO_COMPLETE + " BOOLEAN DEFAULT 0, "
                + COLUMN_COMMENT_HIDDEN_AUTO_COMPLETE + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_DRIVE_SYNC_ID + " TEXT, "
                + AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE ,"
                + AbstractSqlTable.COLUMN_UUID + " TEXT "
                + ");";
        Logger.debug(this, sql);
        db.execSQL(sql);
    }

    @Override
    public synchronized void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        super.onUpgrade(db, oldVersion, newVersion, customizer);
        if (oldVersion <= 12) {
            final String createSqlV12 = "CREATE TABLE " + TABLE_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_PARENT + " TEXT REFERENCES " + TripsTable.COLUMN_NAME + " ON DELETE CASCADE,"
                    + COLUMN_DISTANCE + " DECIMAL(10, 2) DEFAULT 0.00,"
                    + COLUMN_LOCATION + " TEXT,"
                    + COLUMN_DATE + " DATE,"
                    + COLUMN_TIMEZONE + " TEXT,"
                    + COLUMN_COMMENT + " TEXT,"
                    + COLUMN_RATE_CURRENCY + " TEXT NOT NULL, "
                    + COLUMN_RATE + " DECIMAL(10, 2) DEFAULT 0.00"
                    + ");";
            Logger.debug(this, createSqlV12);
            db.execSQL(createSqlV12);

            // Once we create the table, we need to move our "trips" mileage into a single item in the distance table
            final String distanceMigrateBase = "INSERT INTO " + DistanceTable.TABLE_NAME + "(" + DistanceTable.COLUMN_PARENT + ", " + DistanceTable.COLUMN_DISTANCE + ", " + DistanceTable.COLUMN_LOCATION + ", " + DistanceTable.COLUMN_DATE + ", " + DistanceTable.COLUMN_TIMEZONE + ", " + DistanceTable.COLUMN_COMMENT + ", " + DistanceTable.COLUMN_RATE_CURRENCY + ")"
                    + " SELECT " + TripsTable.COLUMN_NAME + ", " + TripsTable.COLUMN_MILEAGE + " , \"\" as " + DistanceTable.COLUMN_LOCATION + ", " + TripsTable.COLUMN_FROM + ", " + TripsTable.COLUMN_FROM_TIMEZONE + " , \"\" as " + DistanceTable.COLUMN_COMMENT + ", ";
            final String distanceMigrateNotNullCurrency = distanceMigrateBase + TripsTable.COLUMN_DEFAULT_CURRENCY + " FROM " + TripsTable.TABLE_NAME + " WHERE " + TripsTable.COLUMN_DEFAULT_CURRENCY + " IS NOT NULL AND " + TripsTable.COLUMN_MILEAGE + " > 0;";
            final String distanceMigrateNullCurrency = distanceMigrateBase + "\"" + userPreferenceManager.get(UserPreference.General.DefaultCurrency) + "\" as " + DistanceTable.COLUMN_RATE_CURRENCY + " FROM " + TripsTable.TABLE_NAME + " WHERE " + TripsTable.COLUMN_DEFAULT_CURRENCY + " IS NULL AND " + TripsTable.COLUMN_MILEAGE + " > 0;";

            Logger.debug(this, distanceMigrateNotNullCurrency);
            Logger.debug(this, distanceMigrateNullCurrency);
            db.execSQL(distanceMigrateNotNullCurrency);
            db.execSQL(distanceMigrateNullCurrency);
        }

        if (oldVersion <= 14) {
            onUpgradeToAddSyncInformation(db, oldVersion, newVersion);
        }

        if (oldVersion <= 18) { //v18 => 19 Changed Trip foreign key from Name to Id, added UUID column

            // adding parent trip id as a foreign key
            final String addTripIdColumn = String.format("ALTER TABLE %s ADD %s INTEGER REFERENCES %s ON DELETE CASCADE",
                    TABLE_NAME, COLUMN_PARENT_TRIP_ID, TripsTable.TABLE_NAME);
            Logger.debug(this, addTripIdColumn);
            db.execSQL(addTripIdColumn);

            final String fillTripId = String.format("UPDATE %s SET %s = ( SELECT %s FROM %s WHERE %s = %s LIMIT 1 )",
                    TABLE_NAME, COLUMN_PARENT_TRIP_ID, TripsTable.COLUMN_ID, TripsTable.TABLE_NAME,
                    TripsTable.COLUMN_NAME, COLUMN_PARENT);
            Logger.debug(this, fillTripId);
            db.execSQL(fillTripId);

            // removing old COLUMN_PARENT column
            final String finalColumns = TextUtils.join(",", new String[]{
                    COLUMN_ID, COLUMN_PARENT_TRIP_ID, COLUMN_DISTANCE, COLUMN_LOCATION, COLUMN_DATE, COLUMN_TIMEZONE,
                    COLUMN_COMMENT, COLUMN_RATE_CURRENCY, COLUMN_RATE, AbstractSqlTable.COLUMN_DRIVE_SYNC_ID,
                    AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED, AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION,
                    AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME});

            final String copyTable = "CREATE TABLE " + TABLE_NAME + "_copy" + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_PARENT_TRIP_ID + " INTEGER REFERENCES " + TripsTable.COLUMN_NAME + " ON DELETE CASCADE,"
                    + COLUMN_DISTANCE + " DECIMAL(10, 2) DEFAULT 0.00,"
                    + COLUMN_LOCATION + " TEXT,"
                    + COLUMN_DATE + " DATE,"
                    + COLUMN_TIMEZONE + " TEXT,"
                    + COLUMN_COMMENT + " TEXT,"
                    + COLUMN_RATE_CURRENCY + " TEXT NOT NULL, "
                    + COLUMN_RATE + " DECIMAL(10, 2) DEFAULT 0.00, "
                    + AbstractSqlTable.COLUMN_DRIVE_SYNC_ID + " TEXT, "
                    + AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED + " BOOLEAN DEFAULT 0, "
                    + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " BOOLEAN DEFAULT 0, "
                    + AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE "
                    + ");";
            Logger.debug(this, copyTable);
            db.execSQL(copyTable);

            final String insertData = String.format("INSERT INTO %s_copy ( %s ) SELECT %s FROM %s ;",
                    TABLE_NAME, finalColumns, finalColumns, TABLE_NAME);
            Logger.debug(this, insertData);
            db.execSQL(insertData);

            final String dropOldTable = String.format("DROP TABLE %s;", TABLE_NAME);
            Logger.debug(this, dropOldTable);
            db.execSQL(dropOldTable);

            final String renameTable = String.format("ALTER TABLE %s_copy RENAME TO %s;", TABLE_NAME, TABLE_NAME);
            Logger.debug(this, renameTable);
            db.execSQL(renameTable);

            onUpgradeToAddUUID(db, oldVersion);
        }

        if (oldVersion <= 19) {
            final String alterDistance = "ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_PAYMENT_METHOD_ID + " INTEGER REFERENCES " + PaymentMethodsTable.TABLE_NAME + " ON DELETE NO ACTION";
            final String alterDistance2 = "ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_LOCATION_HIDDEN_AUTO_COMPLETE + " BOOLEAN DEFAULT 0";
            final String alterDistance3 = "ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_COMMENT_HIDDEN_AUTO_COMPLETE + " BOOLEAN DEFAULT 0";

            Logger.debug(this, alterDistance);
            Logger.debug(this, alterDistance2);
            Logger.debug(this, alterDistance3);

            db.execSQL(alterDistance);
            db.execSQL(alterDistance2);
            db.execSQL(alterDistance3);
        }
    }

    @NonNull
    @Override
    protected Trip getTripFor(@NonNull Distance distance) {
        return distance.getTrip();
    }

}

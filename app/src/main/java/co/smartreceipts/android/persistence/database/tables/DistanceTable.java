package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.tables.adapters.DistanceDatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.keys.DistancePrimaryKey;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.log.Logger;

/**
 * Stores all database operations related to the {@link Distance} model objects
 */
public class DistanceTable extends TripForeignKeyAbstractSqlTable<Distance, Integer> {

    // SQL Definitions:
    public static final String TABLE_NAME = "distance";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PARENT = "parent";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIMEZONE = "timezone";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_RATE = "rate";
    public static final String COLUMN_RATE_CURRENCY = "rate_currency";

    private final UserPreferenceManager userPreferenceManager;

    public DistanceTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull Table<Trip, String> tripsTable, @NonNull UserPreferenceManager userPreferenceManager) {
        super(sqLiteOpenHelper, TABLE_NAME, new DistanceDatabaseAdapter(tripsTable), new DistancePrimaryKey(), COLUMN_PARENT, COLUMN_DATE);
        this.userPreferenceManager = Preconditions.checkNotNull(userPreferenceManager);
    }

    @Override
    public synchronized void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        super.onCreate(db, customizer);
        final String sql = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PARENT + " TEXT REFERENCES " + TripsTable.COLUMN_NAME + " ON DELETE CASCADE,"
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
    }

    @NonNull
    @Override
    protected Trip getTripFor(@NonNull Distance distance) {
        return distance.getTrip();
    }

}

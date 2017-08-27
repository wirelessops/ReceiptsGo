package co.smartreceipts.android.persistence.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.io.File;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.tables.adapters.SyncStateAdapter;
import co.smartreceipts.android.persistence.database.tables.adapters.TripDatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.keys.TripPrimaryKey;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderBy;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.utils.log.Logger;
import wb.android.storage.StorageManager;

public class TripsTable extends AbstractSqlTable<Trip, String> {

    public static final String TABLE_NAME = "trips";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_FROM = "from_date";
    public static final String COLUMN_TO = "to_date";
    public static final String COLUMN_FROM_TIMEZONE = "from_timezone";
    public static final String COLUMN_TO_TIMEZONE = "to_timezone";
    public static final String COLUMN_MILEAGE = "miles_new";
    public static final String COLUMN_COMMENT = "trips_comment";
    public static final String COLUMN_COST_CENTER = "trips_cost_center";
    public static final String COLUMN_DEFAULT_CURRENCY = "trips_default_currency";
    public static final String COLUMN_FILTERS = "trips_filters";
    public static final String COLUMN_PROCESSING_STATUS = "trip_processing_status";

    @SuppressWarnings("unused")
    @Deprecated
    private static final String COLUMN_PRICE = "price"; // Once used but keeping to avoid future name conflicts

    public TripsTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull StorageManager storageManager, @NonNull UserPreferenceManager preferences) {
        super(sqLiteOpenHelper, TABLE_NAME, new TripDatabaseAdapter(storageManager, preferences, new SyncStateAdapter()),
                new TripPrimaryKey(), new OrderBy(TripsTable.COLUMN_TO, true));
    }

    @Override
    public synchronized void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        super.onCreate(db, customizer);
        final String trips = "CREATE TABLE " + getTableName() + " ("
                + COLUMN_NAME + " TEXT PRIMARY KEY, "
                + COLUMN_FROM + " DATE, "
                + COLUMN_TO + " DATE, "
                + COLUMN_FROM_TIMEZONE + " TEXT, "
                + COLUMN_TO_TIMEZONE + " TEXT, "
                + COLUMN_COMMENT + " TEXT, "
                + COLUMN_COST_CENTER + " TEXT, "
                + COLUMN_DEFAULT_CURRENCY + " TEXT, "
                + COLUMN_PROCESSING_STATUS + " TEXT, "
                + COLUMN_FILTERS + " TEXT, "
                + AbstractSqlTable.COLUMN_DRIVE_SYNC_ID + " TEXT, "
                + AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE"
                + ");";
        Logger.debug(this, trips);
        db.execSQL(trips);
    }

    @Override
    public synchronized void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        super.onUpgrade(db, oldVersion, newVersion, customizer);

        if (oldVersion <= 6) { // Fix the database to replace absolute paths with relative ones
            Cursor tripsCursor = null;
            try {
                tripsCursor = db.query(TripsTable.TABLE_NAME, new String[]{ TripsTable.COLUMN_NAME }, null, null, null, null, null);
                if (tripsCursor != null && tripsCursor.moveToFirst()) {
                    final int nameIndex = tripsCursor.getColumnIndex(TripsTable.COLUMN_NAME);
                    do {
                        String absPath = tripsCursor.getString(nameIndex);
                        if (absPath.endsWith(File.separator)) {
                            absPath = absPath.substring(0, absPath.length() - 1);
                        }

                        final String relPath = absPath.substring(absPath.lastIndexOf(File.separatorChar) + 1, absPath.length());
                        Logger.debug("Updating Abs. Trip Path: {} => {}", absPath, relPath);

                        final ContentValues tripValues = new ContentValues(1);
                        tripValues.put(TripsTable.COLUMN_NAME, relPath);
                        if (db.update(TripsTable.TABLE_NAME, tripValues, TripsTable.COLUMN_NAME + " = ?", new String[]{absPath}) == 0) {
                            Logger.error(this, "Trip Update Error Occured");
                        }
                    }
                    while (tripsCursor.moveToNext());
                }
            } finally {
                if (tripsCursor != null) {
                    tripsCursor.close();
                }
            }
        }

        if (oldVersion <= 8) { // Added a timezone column to the trips table
            final String alterTrips1 = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_FROM_TIMEZONE + " TEXT";
            final String alterTrips2 = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_TO_TIMEZONE + " TEXT";

            Logger.debug(this, alterTrips1);
            Logger.debug(this, alterTrips2);

            db.execSQL(alterTrips1);
            db.execSQL(alterTrips2);
        }

        if (oldVersion <= 10) {
            final String alterTrips1 = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_COMMENT + " TEXT";
            final String alterTrips2 = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_DEFAULT_CURRENCY + " TEXT";

            Logger.debug(this, alterTrips1);
            Logger.debug(this, alterTrips2);

            db.execSQL(alterTrips1);
            db.execSQL(alterTrips2);
        }

        if (oldVersion <= 11) { // Added trips filters, payment methods, and mileage table
            final String alterTrips = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_FILTERS + " TEXT";

            Logger.debug(this, alterTrips);

            db.execSQL(alterTrips);
        }

        if (oldVersion <= 12) { //Added better distance tracking, cost center to the trips, and status to trips/receipts
            final String alterTripsWithCostCenter = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_COST_CENTER + " TEXT";
            final String alterTripsWithProcessingStatus = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_PROCESSING_STATUS + " TEXT";

            Logger.debug(this, alterTripsWithCostCenter);
            Logger.debug(this, alterTripsWithProcessingStatus);

            db.execSQL(alterTripsWithCostCenter);
            db.execSQL(alterTripsWithProcessingStatus);

        }

        if (oldVersion <= 14) {
            onUpgradeToAddSyncInformation(db, oldVersion, newVersion);
        }

    }

}

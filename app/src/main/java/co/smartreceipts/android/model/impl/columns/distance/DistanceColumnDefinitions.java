package co.smartreceipts.android.model.impl.columns.distance;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import wb.android.flex.Flex;

/**
 * Provides specific definitions for all {@link co.smartreceipts.android.model.Receipt} {@link co.smartreceipts.android.model.Column}
 * objects
 */
public final class DistanceColumnDefinitions implements ColumnDefinitions<Distance> {


    private enum ActualDefinition {
        LOCATION(R.string.distance_location_field),
        PRICE(R.string.distance_price_field),
        DISTANCE(R.string.distance_distance_field),
        CURRENCY(R.string.dialog_currency_field),
        RATE(R.string.distance_rate_field),
        DATE(R.string.distance_date_field),
        COMMENT(R.string.distance_comment_field);

        private final int mStringResId;

        ActualDefinition(int stringResId) {
            mStringResId = stringResId;
        }

        public final int getStringResId() {
            return mStringResId;
        }

    }


    private final Context mContext;
    private final DatabaseHelper mDB;
    private final UserPreferenceManager mPreferences;
    private final Flex mFlex;
    private final ActualDefinition[] mActualDefinitions;
    private final boolean mAllowSpecialCharacters;

    public DistanceColumnDefinitions(@NonNull Context context, @NonNull PersistenceManager persistenceManager, Flex flex, boolean allowSpecialCharacters) {
        this(context, persistenceManager.getDatabase(), persistenceManager.getPreferenceManager(), flex, allowSpecialCharacters);
    }

    public DistanceColumnDefinitions(@NonNull Context context, @NonNull DatabaseHelper db,
                                     @NonNull UserPreferenceManager preferences, Flex flex, boolean allowSpecialCharacters) {
        mContext = context;
        mDB = db;
        mPreferences = preferences;
        mFlex = flex;
        mAllowSpecialCharacters = allowSpecialCharacters;
        mActualDefinitions = ActualDefinition.values();
    }


    @Override
    public Column<Distance> getColumn(int id, @NonNull String definitionName, @NonNull SyncState syncState, int ignoredCustomOrderId) {
        for (int i = 0; i < mActualDefinitions.length; i++) {
            final ActualDefinition definition = mActualDefinitions[i];
            if (definitionName.equals(getColumnNameFromStringResId(definition.getStringResId()))) {
                return getColumnFromClass(id, definition, definitionName, syncState);
            }
        }
        return null;
    }

    @NonNull
    @Override
    public List<Column<Distance>> getAllColumns() {
        final ArrayList<AbstractColumnImpl<Distance>> columns = new ArrayList<>(mActualDefinitions.length);
        for (int i = 0; i < mActualDefinitions.length; i++) {
            final ActualDefinition definition = mActualDefinitions[i];
            final AbstractColumnImpl<Distance> column = getColumnFromClass(Column.UNKNOWN_ID, definition, getColumnNameFromStringResId(definition.getStringResId()), new DefaultSyncState());
            if (column != null) {
                columns.add(column);
            }
        }
        return new ArrayList<Column<Distance>>(columns);
    }

    @NonNull
    @Override
    public Column<Distance> getDefaultInsertColumn() {
        // Hack for the distance default until we let users dynamically set columns
        return new BlankColumn<>(Column.UNKNOWN_ID, mContext.getString(R.string.column_item_blank), new DefaultSyncState());
    }


    private AbstractColumnImpl<Distance> getColumnFromClass(int id, @NonNull ActualDefinition definition, @NonNull String definitionName, @NonNull SyncState syncState) {
        switch (definition) {
            case LOCATION:
                return new DistanceLocationColumn(id, definitionName, syncState, mContext);
            case PRICE:
                return new DistancePriceColumn(id, definitionName, syncState, mAllowSpecialCharacters);
            case DISTANCE:
                return new DistanceDistanceColumn(id, definitionName, syncState);
            case CURRENCY:
                return new DistanceCurrencyColumn(id, definitionName, syncState);
            case RATE:
                return new DistanceRateColumn(id, definitionName, syncState);
            case DATE:
                return new DistanceDateColumn(id, definitionName, syncState, mContext, mPreferences);
            case COMMENT:
                return new DistanceCommentColumn(id, definitionName, syncState);
            default:
                throw new IllegalArgumentException("Unknown definition type: " + definition);
        }
    }

    private String getColumnNameFromStringResId(int stringResId) {
        if (mFlex != null) {
            return mFlex.getString(mContext, stringResId);
        } else {
            return mContext.getString(stringResId);
        }
    }

}

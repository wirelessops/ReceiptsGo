package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

public class CategoryGroupingReceiptColumnDefinitions implements ColumnDefinitions<Receipt> {

    private enum ActualDefinition {
        NAME(R.string.RECEIPTMENU_FIELD_NAME),
        PRICE_EXCHANGED(R.string.column_item_converted_price_exchange_rate),
        DATE(R.string.RECEIPTMENU_FIELD_DATE),
        REIMBURSABLE(R.string.column_item_reimbursable),
        PICTURED(R.string.column_item_pictured);

        private final int stringResId;

        ActualDefinition(int stringResId) {
            this.stringResId = stringResId;
        }

        @StringRes
        public final int getStringResId() {
            return stringResId;
        }
    }


    private final ActualDefinition[] actualDefinitions;
    private final Context context;
    private final UserPreferenceManager preferences;


    public CategoryGroupingReceiptColumnDefinitions(Context context, UserPreferenceManager preferences) {
        this.context = context;
        this.preferences = preferences;
        this.actualDefinitions = ActualDefinition.values();
    }


    @Override
    public Column<Receipt> getColumn(int id, @NonNull String definitionName, @NonNull SyncState syncState) {
        for (int i = 0; i < actualDefinitions.length; i++) {
            final ActualDefinition definition = actualDefinitions[i];
            if (definitionName.equals(context.getString(definition.getStringResId()))) {
                return getColumnFromClass(id, definition, definitionName, syncState);
            }
        }
        return null;
    }

    @NonNull
    @Override
    public List<Column<Receipt>> getAllColumns() {
        final ArrayList<AbstractColumnImpl<Receipt>> columns = new ArrayList<>(actualDefinitions.length);
        for (int i = 0; i < actualDefinitions.length; i++) {
            final ActualDefinition definition = actualDefinitions[i];
            final AbstractColumnImpl<Receipt> column = getColumnFromClass(Column.UNKNOWN_ID, definition,
                    context.getString(definition.getStringResId()), new DefaultSyncState());
            if (column != null) {
                columns.add(column);
            }
        }
        return new ArrayList<Column<Receipt>>(columns);    }

    @NonNull
    @Override
    public Column<Receipt> getDefaultInsertColumn() {
        return new BlankColumn<>(Column.UNKNOWN_ID, context.getString(R.string.column_item_blank), new DefaultSyncState());
    }

    private AbstractColumnImpl<Receipt> getColumnFromClass(int id, @NonNull ActualDefinition definition,
                                                           @NonNull String definitionName, @NonNull SyncState syncState) {
        switch (definition) {
            case NAME:
                return new ReceiptNameColumn(id, definitionName, syncState);
            case PRICE_EXCHANGED:
                return new ReceiptExchangedPriceColumn(id, definitionName, syncState, context);
            case DATE:
                return new ReceiptDateColumn(id, definitionName, syncState, context, preferences);
            case REIMBURSABLE:
                return new ReceiptIsReimbursableColumn(id, definitionName, syncState, context);
            case PICTURED:
            return new ReceiptIsPicturedColumn(id, definitionName, syncState, context);
            default:
                throw new IllegalArgumentException("Unknown definition type: " + definition);
        }
    }
}

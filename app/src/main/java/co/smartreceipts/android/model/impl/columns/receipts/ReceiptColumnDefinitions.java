package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.comparators.ColumnNameComparator;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.model.impl.columns.SettingUserIdColumn;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import wb.android.flex.Flex;

/**
 * Provides specific definitions for all {@link co.smartreceipts.android.model.Receipt} {@link co.smartreceipts.android.model.Column}
 * objects
 */
public final class ReceiptColumnDefinitions implements ColumnDefinitions<Receipt> {

    private enum ActualDefinition {
        BLANK(R.string.column_item_blank),
        CATEGORY_CODE(R.string.column_item_category_code),
        CATEGORY_NAME(R.string.column_item_category_name),
        USER_ID(R.string.column_item_user_id),
        REPORT_NAME(R.string.column_item_report_name),
        REPORT_START_DATE(R.string.column_item_report_start_date),
        REPORT_END_DATE(R.string.column_item_report_end_date),
        REPORT_COMMENT(R.string.column_item_report_comment),
        REPORT_COST_CENTER(R.string.column_item_report_cost_center),
        IMAGE_FILE_NAME(R.string.column_item_image_file_name),
        IMAGE_PATH(R.string.column_item_image_path),
        COMMENT(R.string.RECEIPTMENU_FIELD_COMMENT),
        CURRENCY(R.string.RECEIPTMENU_FIELD_CURRENCY),
        DATE(R.string.RECEIPTMENU_FIELD_DATE),
        NAME(R.string.RECEIPTMENU_FIELD_NAME),
        PRICE(R.string.RECEIPTMENU_FIELD_PRICE),
        PRICE_EXCHANGED(R.string.column_item_converted_price_exchange_rate),
        TAX(R.string.RECEIPTMENU_FIELD_TAX),
        TAX_EXCHANGED(R.string.column_item_converted_tax_exchange_rate),
        PRICE_PLUS_TAX_EXCHANGED(R.string.column_item_converted_price_plus_tax_exchange_rate),
        EXCHANGE_RATE(R.string.column_item_exchange_rate),
        PICTURED(R.string.column_item_pictured),
        REIMBURSABLE(R.string.column_item_reimbursable, R.string.column_item_deprecated_expensable),
        INDEX(R.string.column_item_index),
        ID(R.string.column_item_id),
        PAYMENT_METHOD(R.string.column_item_payment_method),
        EXTRA_EDITTEXT_1(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1),
        EXTRA_EDITTEXT_2(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2),
        EXTRA_EDITTEXT_3(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3);

        private final int mStringResId;
        private final int mLegacyStringResId;

        ActualDefinition(@StringRes int stringResId) {
            this(stringResId, -1);
        }

        /**
         * Allows us to specify a legacy item that we've updated our name from, since columns are keyed off the name itself (so what happens
         * if we change the column name... probably not the best design here but we'll revisit later)
         *
         * @param stringResId the current id
         * @param legacyStringResId the legacy id
         */
        ActualDefinition(@StringRes int stringResId, @StringRes int legacyStringResId) {
            mStringResId = stringResId;
            mLegacyStringResId = legacyStringResId;
        }

        @StringRes
        public final int getStringResId() {
            return mStringResId;
        }

        @StringRes
        public final int getLegacyStringResId() {
            return mLegacyStringResId;
        }

    }


   @Inject Context context;
   @Inject UserPreferenceManager mPreferences;
   @Inject Flex flex;

   private final ActualDefinition[] mActualDefinitions = ActualDefinition.values();

    @Inject
    public ReceiptColumnDefinitions() {
    }

    @Nullable
    @Override
    public Column<Receipt> getColumn(int id, @NonNull String definitionName, @NonNull SyncState syncState) {
        for (int i = 0; i < mActualDefinitions.length; i++) {
            final ActualDefinition definition = mActualDefinitions[i];
            if (definitionName.equals(getColumnNameFromStringResId(definition.getStringResId()))) {
                return getColumnFromClass(id, definition, definitionName, syncState);
            } else if (definition.getLegacyStringResId() >= 0 && definitionName.equals(getColumnNameFromStringResId(definition.getLegacyStringResId()))) {
                final String newDefinitionName = getColumnNameFromStringResId(definition.getStringResId());
                return getColumnFromClass(id, definition, newDefinitionName, syncState);
            }
        }
        return null;
    }

    @NonNull
    @Override
    public List<Column<Receipt>> getAllColumns() {
        final ArrayList<AbstractColumnImpl<Receipt>> columns = new ArrayList<>(mActualDefinitions.length);
        for (int i = 0; i < mActualDefinitions.length; i++) {
            final ActualDefinition definition = mActualDefinitions[i];
            final AbstractColumnImpl<Receipt> column = getColumnFromClass(Column.UNKNOWN_ID, definition, getColumnNameFromStringResId(definition.getStringResId()), new DefaultSyncState());
            if (column != null) {
                columns.add(column);
            }
        }
        Collections.sort(columns, new ColumnNameComparator<AbstractColumnImpl<Receipt>>());
        return new ArrayList<Column<Receipt>>(columns);
    }

    @NonNull
    @Override
    public Column<Receipt> getDefaultInsertColumn() {
        return new BlankColumn<>(Column.UNKNOWN_ID, getColumnNameFromStringResId(ActualDefinition.BLANK.getStringResId()), new DefaultSyncState());
    }

    public List<Column<Receipt>> getCsvDefaults() {
        // TODO: Re-design how these are added
        final ArrayList<Column<Receipt>> columns = new ArrayList<>();
        columns.add(getColumn(ActualDefinition.CATEGORY_CODE));
        columns.add(getColumn(ActualDefinition.NAME));
        columns.add(getColumn(ActualDefinition.PRICE));
        columns.add(getColumn(ActualDefinition.CURRENCY));
        columns.add(getColumn(ActualDefinition.DATE));
        return columns;

    }

    public List<Column<Receipt>> getPdfDefaults() {
        // TODO: Re-design how these are added
        final ArrayList<Column<Receipt>> columns = new ArrayList<>();
        columns.add(getColumn(ActualDefinition.NAME));
        columns.add(getColumn(ActualDefinition.PRICE));
        columns.add(getColumn(ActualDefinition.DATE));
        columns.add(getColumn(ActualDefinition.CATEGORY_NAME));
        columns.add(getColumn(ActualDefinition.REIMBURSABLE));
        columns.add(getColumn(ActualDefinition.PICTURED));
        return columns;
    }

    private Column<Receipt> getColumn(@NonNull ActualDefinition actualDefinition) {
        return getColumnFromClass(Column.UNKNOWN_ID, actualDefinition, getColumnNameFromStringResId(actualDefinition.getStringResId()), new DefaultSyncState());
    }

    @Nullable
    private AbstractColumnImpl<Receipt> getColumnFromClass(int id, @NonNull ActualDefinition definition, @NonNull String definitionName, @NonNull SyncState syncState) {
        if (TextUtils.isEmpty(definitionName)) {
            // Exit early if we have no name (i.e. it's an undefined extra)
            return null;
        }
        switch (definition) {
            case BLANK:
                return new BlankColumn<>(id, definitionName, syncState);
            case CATEGORY_CODE:
                return new ReceiptCategoryCodeColumn(id, definitionName, syncState);
            case CATEGORY_NAME:
                return new ReceiptCategoryNameColumn(id, definitionName, syncState);
            case USER_ID:
                return new SettingUserIdColumn<>(id, definitionName, syncState, mPreferences);
            case REPORT_NAME:
                return new ReportNameColumn(id, definitionName, syncState);
            case REPORT_START_DATE:
                return new ReportStartDateColumn(id, definitionName, syncState, context, mPreferences);
            case REPORT_END_DATE:
                return new ReportEndDateColumn(id, definitionName, syncState, context, mPreferences);
            case REPORT_COMMENT:
                return new ReportCommentColumn(id, definitionName, syncState);
            case REPORT_COST_CENTER:
                return new ReportCostCenterColumn(id, definitionName, syncState);
            case IMAGE_FILE_NAME:
                return new ReceiptFileNameColumn(id, definitionName, syncState);
            case IMAGE_PATH:
                return new ReceiptFilePathColumn(id, definitionName, syncState);
            case COMMENT:
                return new ReceiptCommentColumn(id, definitionName, syncState);
            case CURRENCY:
                return new ReceiptCurrencyCodeColumn(id, definitionName, syncState);
            case DATE:
                return new ReceiptDateColumn(id, definitionName, syncState, context, mPreferences);
            case NAME:
                return new ReceiptNameColumn(id, definitionName, syncState);
            case PRICE:
                return new ReceiptPriceColumn(id, definitionName, syncState);
            case PRICE_EXCHANGED:
                return new ReceiptExchangedPriceColumn(id, definitionName, syncState, context);
            case TAX:
                return new ReceiptTaxColumn(id, definitionName, syncState);
            case TAX_EXCHANGED:
                return new ReceiptExchangedTaxColumn(id, definitionName, syncState, context);
            case PRICE_PLUS_TAX_EXCHANGED:
                return new ReceiptNetExchangedPricePlusTaxColumn(id, definitionName, syncState, context, mPreferences);
            case EXCHANGE_RATE:
                return new ReceiptExchangeRateColumn(id, definitionName, syncState);
            case PICTURED:
                return new ReceiptIsPicturedColumn(id, definitionName, syncState, context);
            case REIMBURSABLE:
                return new ReceiptIsReimbursableColumn(id, definitionName, syncState, context);
            case INDEX:
                return new ReceiptIndexColumn(id, definitionName, syncState);
            case ID:
                return new ReceiptIdColumn(id, definitionName, syncState);
            case PAYMENT_METHOD:
                return new ReceiptPaymentMethodColumn(id, definitionName, syncState);
            case EXTRA_EDITTEXT_1:
                return new ReceiptExtra1Column(id, definitionName, syncState);
            case EXTRA_EDITTEXT_2:
                return new ReceiptExtra2Column(id, definitionName, syncState);
            case EXTRA_EDITTEXT_3:
                return new ReceiptExtra3Column(id, definitionName, syncState);
            default:
                throw new IllegalArgumentException("Unknown definition type: " + definition);
        }
    }

    private String getColumnNameFromStringResId(int stringResId) {
        if (flex != null) {
            return flex.getString(context, stringResId);
        } else {
            return context.getString(stringResId);
        }
    }

}

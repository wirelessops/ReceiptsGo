package co.smartreceipts.android.model.impl.columns.receipts

import android.support.annotation.StringRes
import co.smartreceipts.android.R
import co.smartreceipts.android.model.*
import co.smartreceipts.android.model.comparators.ColumnNameComparator
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.model.impl.columns.BlankColumn
import co.smartreceipts.android.model.impl.columns.SettingUserIdColumn
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions.ActualDefinition.*
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.sync.model.SyncState
import co.smartreceipts.android.sync.model.impl.DefaultSyncState
import co.smartreceipts.android.workers.reports.ReportResourcesManager
import java.util.*
import javax.inject.Inject

/**
 * Provides specific definitions for all [co.smartreceipts.android.model.Receipt] [co.smartreceipts.android.model.Column]
 * objects
 */
class ReceiptColumnDefinitions @Inject
constructor(
    private val reportResourcesManager: ReportResourcesManager,
    private val preferences: UserPreferenceManager
) : ColumnDefinitions<Receipt>, ColumnFinder {
    private val actualDefinitions = values()

    /**
     * Note: Column types must be unique, because they are saved to the DB
     * Column type must be >= 0
     */
    enum class ActualDefinition : ActualColumnDefinition {
        BLANK(0, R.string.column_item_blank, R.string.original_column_item_blank_en_us_name),
        CATEGORY_CODE(1, R.string.column_item_category_code, R.string.original_column_item_category_code_en_us_name),
        CATEGORY_NAME(2, R.string.column_item_category_name, R.string.original_column_item_category_name_en_us_name),
        USER_ID(3, R.string.column_item_user_id, R.string.original_column_item_user_id_en_us_name),
        REPORT_NAME(4, R.string.column_item_report_name, R.string.original_column_item_report_name_en_us_name),
        REPORT_START_DATE(5, R.string.column_item_report_start_date, R.string.original_column_item_report_start_date_en_us_name),
        REPORT_END_DATE(6, R.string.column_item_report_end_date, R.string.original_column_item_report_end_date_en_us_name),
        REPORT_COMMENT(7, R.string.column_item_report_comment, R.string.original_column_item_report_comment_en_us_name),
        REPORT_COST_CENTER(8, R.string.column_item_report_cost_center, R.string.original_column_item_report_cost_center_en_us_name),
        IMAGE_FILE_NAME(9, R.string.column_item_image_file_name, R.string.original_column_item_image_file_name_en_us_name),
        IMAGE_PATH(10, R.string.column_item_image_path, R.string.original_column_item_image_path_en_us_name),
        COMMENT(11, R.string.RECEIPTMENU_FIELD_COMMENT, R.string.original_column_RECEIPTMENU_FIELD_COMMENT_en_us_name),
        CURRENCY(12, R.string.RECEIPTMENU_FIELD_CURRENCY, R.string.original_column_RECEIPTMENU_FIELD_CURRENCY_en_us_name),
        DATE(13, R.string.RECEIPTMENU_FIELD_DATE, R.string.original_column_RECEIPTMENU_FIELD_DATE_en_us_name),
        NAME(14, R.string.RECEIPTMENU_FIELD_NAME, R.string.original_column_RECEIPTMENU_FIELD_NAME_en_us_name),
        PRICE(15, R.string.RECEIPTMENU_FIELD_PRICE, R.string.original_column_RECEIPTMENU_FIELD_PRICE_en_us_name),
        PRICE_MINUS_TAX(16, R.string.column_item_receipt_price_minus_tax),
        PRICE_EXCHANGED(17, R.string.column_item_converted_price_exchange_rate, R.string.original_column_item_converted_price_exchange_rate_en_us_name),
        TAX(18, R.string.RECEIPTMENU_FIELD_TAX, R.string.original_column_RECEIPTMENU_FIELD_TAX_en_us_name),
        TAX_EXCHANGED(19, R.string.column_item_converted_tax_exchange_rate, R.string.original_column_item_converted_tax_exchange_rate_en_us_name),
        PRICE_PLUS_TAX_EXCHANGED(20, R.string.column_item_converted_price_plus_tax_exchange_rate, R.string.original_column_item_converted_price_plus_tax_exchange_rate_en_us_name),
        PRICE_MINUS_TAX_EXCHANGED(21, R.string.column_item_converted_price_minus_tax_exchange_rate),
        EXCHANGE_RATE(22, R.string.column_item_exchange_rate, R.string.original_column_item_exchange_rate_en_us_name),
        PICTURED(23, R.string.column_item_pictured, R.string.original_column_item_pictured_en_us_name),
        REIMBURSABLE(24, R.string.column_item_reimbursable, R.string.original_column_item_reimbursable_en_us_name, R.string.column_item_deprecated_expensable),
        INDEX(25, R.string.column_item_index, R.string.original_column_item_index_en_us_name),
        ID(26, R.string.column_item_id, R.string.original_column_item_id_en_us_name),
        PAYMENT_METHOD(27, R.string.column_item_payment_method),

        EXTRA_EDITTEXT_1(100, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1),
        EXTRA_EDITTEXT_2(101, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2),
        EXTRA_EDITTEXT_3(102, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3);

        private val columnType: Int
        private val stringResId: Int
        private val legacyStringResIds: MutableList<Int>

        /**
         * Allows us to specify a legacy item that we've updated our name from, since columns are keyed off the name itself (so what happens
         * if we change the column name... probably not the best design here but we'll revisit later)
         *
         * @param columnType         the type number of the column
         * @param stringResId        the current id
         * @param legacyStringResIds the list of legacy id
         */
        constructor(columnType: Int, @StringRes stringResId: Int, @StringRes vararg legacyStringResIds: Int) {
            this.columnType = columnType
            this.stringResId = stringResId
            this.legacyStringResIds = ArrayList()

            for (legacyStringResId in legacyStringResIds) {
                this.legacyStringResIds.add(legacyStringResId)
            }

        }

        override fun getColumnType() = columnType

        override fun getColumnHeaderId() = stringResId

        fun getLegacyStringResIds() = legacyStringResIds

    }

    fun getCsvDefaults(): List<Column<Receipt>> =
        listOf(
            getColumnFromDefinition(DATE),
            getColumnFromDefinition(NAME),
            getColumnFromDefinition(PRICE),
            getColumnFromDefinition(CURRENCY),
            getColumnFromDefinition(CATEGORY_NAME),
            getColumnFromDefinition(CATEGORY_CODE),
            getColumnFromDefinition(COMMENT),
            getColumnFromDefinition(REIMBURSABLE)
        )

    fun getPdfDefaults(): List<Column<Receipt>> =
        listOf(
            getColumnFromDefinition(DATE),
            getColumnFromDefinition(NAME),
            getColumnFromDefinition(PRICE),
            getColumnFromDefinition(CURRENCY),
            getColumnFromDefinition(CATEGORY_NAME),
            getColumnFromDefinition(REIMBURSABLE)
        )

    override fun getColumn(
        id: Int,
        columnType: Int,
        syncState: SyncState,
        customOrderId: Long
    ): Column<Receipt> {
        for (definition in actualDefinitions) {
            if (columnType == definition.columnType) {
                return getColumnFromDefinition(definition, id, syncState, customOrderId)
            }
        }

        throw IllegalArgumentException("Unknown column type: $columnType")
    }

    override fun getAllColumns(): List<Column<Receipt>> {
        val columns = ArrayList<AbstractColumnImpl<Receipt>>()
        for (definition in actualDefinitions) {

            // don't add column if column name is empty (useful for flex cases)
            if (!reportResourcesManager.getFlexString(definition.columnHeaderId).isEmpty()) {

                val column = getColumnFromDefinition(definition, Column.UNKNOWN_ID, DefaultSyncState())
                columns.add(column)
            }

        }
        Collections.sort(columns, ColumnNameComparator(reportResourcesManager))
        return ArrayList<Column<Receipt>>(columns)
    }

    override fun getDefaultInsertColumn(): Column<Receipt> =
        BlankColumn(Column.UNKNOWN_ID, DefaultSyncState(), java.lang.Long.MAX_VALUE)

    override fun getColumnTypeByHeaderValue(header: String): Int {

        for (actualDefinition in actualDefinitions) {
            if (reportResourcesManager.getFlexString(actualDefinition.columnHeaderId) == header) {
                return actualDefinition.columnType
            }
            for (legacyStringResId in actualDefinition.getLegacyStringResIds()) {
                if (legacyStringResId > 0 && reportResourcesManager.getFlexString(legacyStringResId) == header) {
                    return actualDefinition.columnType
                }
            }
        }

        return -1
    }

    fun getColumnFromDefinition(
        definition: ActualDefinition,
        id: Int = Column.UNKNOWN_ID,
        syncState: SyncState = DefaultSyncState(),
        customOrderId: Long = 0
    ): AbstractColumnImpl<Receipt> {
        val localizedContext = reportResourcesManager.getLocalizedContext()

        return when (definition) {
            BLANK -> BlankColumn(id, syncState, customOrderId)
            CATEGORY_CODE -> ReceiptCategoryCodeColumn(id, syncState, customOrderId)
            CATEGORY_NAME -> ReceiptCategoryNameColumn(id, syncState, customOrderId)
            USER_ID -> SettingUserIdColumn(id, syncState, preferences, customOrderId)
            REPORT_NAME -> ReportNameColumn(id, syncState, customOrderId)
            REPORT_START_DATE -> ReportStartDateColumn(id, syncState, localizedContext, preferences, customOrderId)
            REPORT_END_DATE -> ReportEndDateColumn(id, syncState, localizedContext, preferences, customOrderId)
            REPORT_COMMENT -> ReportCommentColumn(id, syncState, customOrderId)
            REPORT_COST_CENTER -> ReportCostCenterColumn(id, syncState, customOrderId)
            IMAGE_FILE_NAME -> ReceiptFileNameColumn(id, syncState, customOrderId)
            IMAGE_PATH -> ReceiptFilePathColumn(id, syncState, customOrderId)
            COMMENT -> ReceiptCommentColumn(id, syncState, customOrderId)
            CURRENCY -> ReceiptCurrencyCodeColumn(id, syncState, customOrderId)
            DATE -> ReceiptDateColumn(id, syncState, localizedContext, preferences, customOrderId)
            NAME -> ReceiptNameColumn(id, syncState, customOrderId)
            PRICE -> ReceiptPriceColumn(id, syncState, customOrderId)
            PRICE_MINUS_TAX -> ReceiptPriceMinusTaxColumn(id, syncState, preferences, customOrderId)
            PRICE_EXCHANGED -> ReceiptExchangedPriceColumn(id, syncState, localizedContext, customOrderId)
            TAX -> ReceiptTaxColumn(id, syncState, customOrderId)
            TAX_EXCHANGED -> ReceiptExchangedTaxColumn(id, syncState, localizedContext, customOrderId)
            PRICE_PLUS_TAX_EXCHANGED -> ReceiptNetExchangedPricePlusTaxColumn(id, syncState, localizedContext, preferences, customOrderId)
            PRICE_MINUS_TAX_EXCHANGED -> ReceiptNetExchangedPriceMinusTaxColumn(id, syncState, localizedContext, preferences, customOrderId)
            EXCHANGE_RATE -> ReceiptExchangeRateColumn(id, syncState, customOrderId)
            PICTURED -> ReceiptIsPicturedColumn(id, syncState, localizedContext, customOrderId)
            REIMBURSABLE -> ReceiptIsReimbursableColumn(id, syncState, localizedContext, customOrderId)
            INDEX -> ReceiptIndexColumn(id, syncState, customOrderId)
            ID -> ReceiptIdColumn(id, syncState, customOrderId)
            PAYMENT_METHOD -> ReceiptPaymentMethodColumn(id, syncState, customOrderId)
            EXTRA_EDITTEXT_1 -> ReceiptExtra1Column(id, syncState, customOrderId)
            EXTRA_EDITTEXT_2 -> ReceiptExtra2Column(id, syncState, customOrderId)
            EXTRA_EDITTEXT_3 -> ReceiptExtra3Column(id, syncState, customOrderId)
        }
    }

}

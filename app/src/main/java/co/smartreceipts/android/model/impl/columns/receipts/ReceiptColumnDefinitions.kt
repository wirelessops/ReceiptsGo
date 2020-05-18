package co.smartreceipts.android.model.impl.columns.receipts

import androidx.annotation.StringRes
import co.smartreceipts.android.R
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.*
import co.smartreceipts.android.model.comparators.ColumnNameComparator
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.android.model.impl.columns.BlankColumn
import co.smartreceipts.android.model.impl.columns.SettingUserIdColumn
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions.ActualDefinition.*
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.core.sync.model.SyncState
import co.smartreceipts.core.sync.model.impl.DefaultSyncState
import co.smartreceipts.android.workers.reports.ReportResourcesManager
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Provides specific definitions for all [Receipt] [Column] objects
 */
class ReceiptColumnDefinitions @Inject constructor(
    private val reportResourcesManager: ReportResourcesManager,
    private val preferences: UserPreferenceManager,
    private val dateFormatter: DateFormatter
) : ColumnDefinitions<Receipt>, ColumnFinder {

    private val actualDefinitions = values()

    /**
     * Note: Column types must be unique, because they are saved to the DB
     * Column type must be >= 0
     */
    enum class ActualDefinition(override val columnType: Int,
                                @StringRes override val columnHeaderId: Int,
                                @StringRes vararg legacyHeaderIds: Int
    ) : ActualColumnDefinition {
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
        PRICE_MINUS_TAX(16, R.string.column_item_receipt_price_minus_tax, R.string.original_column_item_receipt_price_minus_tax_en_us_name),
        PRICE_EXCHANGED(17, R.string.column_item_converted_price_exchange_rate, R.string.original_column_item_converted_price_exchange_rate_en_us_name),
        TAX(18, R.string.RECEIPTMENU_FIELD_TAX, R.string.original_column_RECEIPTMENU_FIELD_TAX_en_us_name),
        TAX_EXCHANGED(19, R.string.column_item_converted_tax_exchange_rate, R.string.original_column_item_converted_tax_exchange_rate_en_us_name),
        PRICE_PLUS_TAX_EXCHANGED(20, R.string.column_item_converted_price_plus_tax_exchange_rate, R.string.original_column_item_converted_price_plus_tax_exchange_rate_en_us_name),
        PRICE_MINUS_TAX_EXCHANGED(21, R.string.column_item_converted_price_minus_tax_exchange_rate, R.string.original_column_item_converted_price_minus_tax_exchange_rate_en_us_name),
        EXCHANGE_RATE(22, R.string.column_item_exchange_rate, R.string.original_column_item_exchange_rate_en_us_name),
        PICTURED(23, R.string.column_item_pictured, R.string.original_column_item_pictured_en_us_name),
        REIMBURSABLE(24, R.string.column_item_reimbursable, R.string.original_column_item_reimbursable_en_us_name, R.string.column_item_deprecated_expensable),
        INDEX(25, R.string.column_item_index, R.string.original_column_item_index_en_us_name),
        ID(26, R.string.column_item_id, R.string.original_column_item_id_en_us_name),
        PAYMENT_METHOD(27, R.string.column_item_payment_method, R.string.original_column_item_payment_method_en_us_name),
        PRICE_WITH_CURRENCY(28, R.string.RECEIPTMENU_FIELD_PRICE_WITH_CURRENCY),
        IMAGE_HASH(29, R.string.column_item_image_hash, R.string.original_column_item_image_hash_en_us_name),
        TAX1(30, R.string.pref_receipt_tax1_name_defaultValue),
        TAX2(31, R.string.pref_receipt_tax2_name_defaultValue),

        EXTRA_EDITTEXT_1(100, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1),
        EXTRA_EDITTEXT_2(101, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2),
        EXTRA_EDITTEXT_3(102, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3);


        val legacyStringResIds: MutableList<Int>

        /**
         * Allows us to specify a legacy item that we've updated our name from, since columns are keyed off the name itself (so what happens
         * if we change the column name... probably not the best design here but we'll revisit later)
         */
        init {
            this.legacyStringResIds = ArrayList()
            for (legacyHeaderResId in legacyHeaderIds) {
                this.legacyStringResIds.add(legacyHeaderResId)
            }
        }
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
        customOrderId: Long,
        uuid: UUID
    ): Column<Receipt> {
        for (definition in actualDefinitions) {
            if (columnType == definition.columnType) {
                return getColumnFromDefinition(definition, id, syncState, customOrderId, uuid)
            }
        }

        throw IllegalArgumentException("Unknown column type: $columnType")
    }

    override fun getAllColumns(): List<Column<Receipt>> {
        val columns = ArrayList<AbstractColumnImpl<Receipt>>()
        for (definition in actualDefinitions) {

            // don't add column if column name is empty (useful for flex cases)
            if (reportResourcesManager.getFlexString(definition.columnHeaderId).isNotEmpty()) {

                val column = getColumnFromDefinition(definition)
                columns.add(column)
            }

        }
        Collections.sort(columns, ColumnNameComparator(reportResourcesManager))
        return ArrayList<Column<Receipt>>(columns)
    }

    override fun getDefaultInsertColumn(): Column<Receipt> =
        BlankColumn(Keyed.MISSING_ID, DefaultSyncState(), java.lang.Long.MAX_VALUE, Keyed.MISSING_UUID)

    override fun getColumnTypeByHeaderValue(header: String): Int {

        for (actualDefinition in actualDefinitions) {
            if (reportResourcesManager.getFlexString(actualDefinition.columnHeaderId) == header) {
                return actualDefinition.columnType
            }
            for (legacyStringResId in actualDefinition.legacyStringResIds) {
                if (legacyStringResId > 0 && reportResourcesManager.getFlexString(legacyStringResId) == header) {
                    return actualDefinition.columnType
                }
            }
        }

        return -1
    }

    fun getColumnFromDefinition(
        definition: ActualDefinition,
        id: Int = Keyed.MISSING_ID,
        syncState: SyncState = DefaultSyncState(),
        customOrderId: Long = 0,
        uuid: UUID = Keyed.MISSING_UUID
    ): AbstractColumnImpl<Receipt> {
        val localizedContext = reportResourcesManager.getLocalizedContext()

        return when (definition) {
            BLANK -> BlankColumn(id, syncState, customOrderId, uuid)
            CATEGORY_CODE -> ReceiptCategoryCodeColumn(id, syncState, customOrderId, uuid)
            CATEGORY_NAME -> ReceiptCategoryNameColumn(id, syncState, customOrderId, uuid)
            USER_ID -> SettingUserIdColumn(id, syncState, preferences, customOrderId, uuid)
            REPORT_NAME -> ReportNameColumn(id, syncState, customOrderId, uuid)
            REPORT_START_DATE -> ReportStartDateColumn(id, syncState, dateFormatter, customOrderId, uuid)
            REPORT_END_DATE -> ReportEndDateColumn(id, syncState, dateFormatter, customOrderId, uuid)
            REPORT_COMMENT -> ReportCommentColumn(id, syncState, customOrderId, uuid)
            REPORT_COST_CENTER -> ReportCostCenterColumn(id, syncState, customOrderId, uuid)
            IMAGE_FILE_NAME -> ReceiptFileNameColumn(id, syncState, customOrderId, uuid)
            IMAGE_PATH -> ReceiptFilePathColumn(id, syncState, customOrderId, uuid)
            COMMENT -> ReceiptCommentColumn(id, syncState, customOrderId, uuid)
            CURRENCY -> ReceiptCurrencyCodeColumn(id, syncState, customOrderId, uuid)
            DATE -> ReceiptDateColumn(id, syncState, dateFormatter, customOrderId, uuid)
            NAME -> ReceiptNameColumn(id, syncState, customOrderId, uuid)
            PRICE -> ReceiptPriceColumn(id, syncState, customOrderId, uuid)
            PRICE_MINUS_TAX -> ReceiptPriceMinusTaxColumn(id, syncState, preferences, customOrderId, uuid)
            PRICE_EXCHANGED -> ReceiptExchangedPriceColumn(id, syncState, localizedContext, customOrderId, uuid)
            PRICE_WITH_CURRENCY -> ReceiptPriceWithCurrencyColumn(id, syncState, customOrderId, uuid)
            TAX -> ReceiptTaxColumn(id, syncState, customOrderId, uuid)
            TAX1 -> ReceiptTax1Column(id, syncState, customOrderId, uuid)
            TAX2 -> ReceiptTax2Column(id, syncState, customOrderId, uuid)
            TAX_EXCHANGED -> ReceiptExchangedTaxColumn(id, syncState, localizedContext, customOrderId, uuid)
            PRICE_PLUS_TAX_EXCHANGED -> ReceiptNetExchangedPricePlusTaxColumn(id, syncState, localizedContext, preferences, customOrderId, uuid)
            PRICE_MINUS_TAX_EXCHANGED -> ReceiptNetExchangedPriceMinusTaxColumn(id, syncState, localizedContext, preferences, customOrderId, uuid)
            EXCHANGE_RATE -> ReceiptExchangeRateColumn(id, syncState, customOrderId, uuid)
            PICTURED -> ReceiptIsPicturedColumn(id, syncState, localizedContext, customOrderId, uuid)
            REIMBURSABLE -> ReceiptIsReimbursableColumn(id, syncState, localizedContext, customOrderId, uuid)
            INDEX -> ReceiptIndexColumn(id, syncState, customOrderId, uuid)
            ID -> ReceiptIdColumn(id, syncState, customOrderId, uuid)
            PAYMENT_METHOD -> ReceiptPaymentMethodColumn(id, syncState, customOrderId, uuid)
            IMAGE_HASH -> ReceiptFileHashColumn(id, syncState, customOrderId, uuid)
            EXTRA_EDITTEXT_1 -> ReceiptExtra1Column(id, syncState, customOrderId, uuid)
            EXTRA_EDITTEXT_2 -> ReceiptExtra2Column(id, syncState, customOrderId, uuid)
            EXTRA_EDITTEXT_3 -> ReceiptExtra3Column(id, syncState, customOrderId, uuid)
        }
    }

}

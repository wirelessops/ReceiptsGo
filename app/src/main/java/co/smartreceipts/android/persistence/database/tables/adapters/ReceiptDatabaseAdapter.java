package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.io.File;
import java.math.BigDecimal;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.sync.model.SyncState;
import wb.android.storage.StorageManager;

/**
 * Implements the {@link DatabaseAdapter} contract for the {@link ReceiptsTable}
 */
public final class ReceiptDatabaseAdapter implements SelectionBackedDatabaseAdapter<Receipt, PrimaryKey<Receipt, Integer>, Trip> {

    private final Table<Trip, String> mTripsTable;
    private final Table<PaymentMethod, Integer> mPaymentMethodTable;
    private final Table<Category, Integer> mCategoriesTable;
    private final StorageManager mStorageManager;
    private final SyncStateAdapter mSyncStateAdapter;

    public ReceiptDatabaseAdapter(@NonNull Table<Trip, String> tripsTable, @NonNull Table<PaymentMethod, Integer> paymentMethodTable,
                                  @NonNull Table<Category, Integer> categoriesTable, @NonNull StorageManager storageManager) {
        this(tripsTable, paymentMethodTable, categoriesTable, storageManager, new SyncStateAdapter());
    }

    public ReceiptDatabaseAdapter(@NonNull Table<Trip, String> tripsTable, @NonNull Table<PaymentMethod, Integer> paymentMethodTable,
                                  @NonNull Table<Category, Integer> categoriesTable, @NonNull StorageManager storageManager,
                                  @NonNull SyncStateAdapter syncStateAdapter) {
        mTripsTable = Preconditions.checkNotNull(tripsTable);
        mPaymentMethodTable = Preconditions.checkNotNull(paymentMethodTable);
        mCategoriesTable = Preconditions.checkNotNull(categoriesTable);
        mStorageManager = Preconditions.checkNotNull(storageManager);
        mSyncStateAdapter = Preconditions.checkNotNull(syncStateAdapter);
    }

    @NonNull
    @Override
    public Receipt read(@NonNull Cursor cursor) {
        final int parentIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_PARENT);
        final Trip trip = mTripsTable.findByPrimaryKey(cursor.getString(parentIndex)).blockingGet();
        return readForSelection(cursor, trip, true);
    }


    @NonNull
    @Override
    public Receipt readForSelection(@NonNull Cursor cursor, @NonNull Trip trip, boolean isDescending) {

        final int idIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_ID);
        final int pathIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_PATH);
        final int nameIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_NAME);
        final int categoryIdIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_CATEGORY_ID);
        final int priceIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_PRICE);
        final int taxIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_TAX);
        final int exchangeRateIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_EXCHANGE_RATE);
        final int dateIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_DATE);
        final int timeZoneIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_TIMEZONE);
        final int commentIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_COMMENT);
        final int reimbursableIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_REIMBURSABLE);
        final int currencyIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_ISO4217);
        final int fullpageIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE);
        final int paymentMethodIdIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID);
        final int extra_edittext_1_Index = cursor.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1);
        final int extra_edittext_2_Index = cursor.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2);
        final int extra_edittext_3_Index = cursor.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3);
        final int orderIdIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_CUSTOM_ORDER_ID);

        final int id = cursor.getInt(idIndex);
        final String path = cursor.getString(pathIndex);
        final String name = cursor.getString(nameIndex);

        final int categoryId = cursor.getInt(categoryIdIndex);
        final double priceDouble = cursor.getDouble(priceIndex);
        final double taxDouble = cursor.getDouble(taxIndex);
        final double exchangeRateDouble = cursor.getDouble(exchangeRateIndex);
        final String priceString = cursor.getString(priceIndex);
        final String taxString = cursor.getString(taxIndex);
        final String exchangeRateString = cursor.getString(exchangeRateIndex);
        final long date = cursor.getLong(dateIndex);
        final String timezone = (timeZoneIndex > 0) ? cursor.getString(timeZoneIndex) : null;
        final String possiblyNullComment = cursor.getString(commentIndex);
        final String comment = possiblyNullComment != null ? possiblyNullComment : "";
        final boolean reimbursable = cursor.getInt(reimbursableIndex) > 0;
        final String currency = cursor.getString(currencyIndex);
        final boolean fullpage = !(cursor.getInt(fullpageIndex) > 0);
        final int paymentMethodId = cursor.getInt(paymentMethodIdIndex);
        final String extra_edittext_1 = cursor.getString(extra_edittext_1_Index);
        final String extra_edittext_2 = cursor.getString(extra_edittext_2_Index);
        final String extra_edittext_3 = cursor.getString(extra_edittext_3_Index);
        final int orderId = cursor.getInt(orderIdIndex);
        File file = null;
        if (!TextUtils.isEmpty(path) && !DatabaseHelper.NO_DATA.equals(path)) {
            file = mStorageManager.getFile(trip.getDirectory(), path);
        }
        final SyncState syncState = mSyncStateAdapter.read(cursor);

        // TODO: How to use JOINs w/o blocking
        final Category category = mCategoriesTable.findByPrimaryKey(categoryId)
                .map(Optional::of)
                .onErrorReturn(ignored -> Optional.absent())
                .blockingGet()
                .orNull();

        final PaymentMethod paymentMethod = mPaymentMethodTable.findByPrimaryKey(paymentMethodId)
                        .map(Optional::of)
                        .onErrorReturn(ignored -> Optional.absent())
                        .blockingGet()
                        .orNull();

        final int index = isDescending ? cursor.getCount() - cursor.getPosition() : cursor.getPosition() + 1;

        final ReceiptBuilderFactory builder = new ReceiptBuilderFactory(id);
        builder.setTrip(trip)
                .setName(name)
                .setFile(file)
                .setDate(date)
                .setTimeZone(timezone)
                .setComment(comment)
                .setIsReimbursable(reimbursable)
                .setCurrency(currency)
                .setIsFullPage(fullpage)
                .setIndex(index)
                .setExtraEditText1(extra_edittext_1)
                .setExtraEditText2(extra_edittext_2)
                .setExtraEditText3(extra_edittext_3)
                .setSyncState(syncState)
                .setOrderId(orderId);

        if (category != null) {
            builder.setCategory(category);
        }

        if (paymentMethod != null) {
            builder.setPaymentMethod(paymentMethod);
        }


        /*
         * Please note that a very frustrating bug exists here. Android cursors only return the first 6
         * characters of a price string if that string contains a '.' character. It returns all of them
         * if not. This means we'll break for prices over 5 digits unless we are using a comma separator, 
         * which we'd do in the EU. Stupid check below to un-break this. Stupid Android.
         *
         * TODO: Longer term, everything should be saved with a decimal point
         * https://code.google.com/p/android/issues/detail?id=22219
         */
        if (!TextUtils.isEmpty(priceString) && priceString.contains(",")) {
            builder.setPrice(priceString);
        } else {
            builder.setPrice(priceDouble);
        }
        if (!TextUtils.isEmpty(taxString) && taxString.contains(",")) {
            builder.setTax(taxString);
        } else {
            builder.setTax(taxDouble);
        }
        final ExchangeRateBuilderFactory exchangeRateBuilder = new ExchangeRateBuilderFactory().setBaseCurrency(currency);
        if (!TextUtils.isEmpty(exchangeRateString) && exchangeRateString.contains(",")) {
            exchangeRateBuilder.setRate(trip.getTripCurrency(), exchangeRateString);
        } else {
            exchangeRateBuilder.setRate(trip.getTripCurrency(), exchangeRateDouble);
        }
        builder.setExchangeRate(exchangeRateBuilder.build());

        return builder.build();
    }

    @NonNull
    @Override
    public ContentValues write(@NonNull Receipt receipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final ContentValues values = new ContentValues();

        // Add core data
        values.put(ReceiptsTable.COLUMN_PARENT, receipt.getTrip().getName());
        values.put(ReceiptsTable.COLUMN_NAME, receipt.getName().trim());
        values.put(ReceiptsTable.COLUMN_CATEGORY_ID, receipt.getCategory().getId());
        values.put(ReceiptsTable.COLUMN_DATE, receipt.getDate().getTime());
        values.put(ReceiptsTable.COLUMN_TIMEZONE, receipt.getTimeZone().getID());
        values.put(ReceiptsTable.COLUMN_COMMENT, receipt.getComment());
        values.put(ReceiptsTable.COLUMN_ISO4217, receipt.getPrice().getCurrencyCode());
        values.put(ReceiptsTable.COLUMN_REIMBURSABLE, receipt.isReimbursable());
        values.put(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE, !receipt.isFullPage());

        // Add file
        final File file = receipt.getFile();
        if (file != null) {
            values.put(ReceiptsTable.COLUMN_PATH, file.getName());
        } else {
            values.put(ReceiptsTable.COLUMN_PATH, (String) null);
        }

        // Add payment method if one exists
        if (receipt.getPaymentMethod() != null) {
            values.put(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID, receipt.getPaymentMethod().getId());
        }

        // Note: We replace the commas here with decimals to avoid database bugs around parsing decimal values
        // TODO: Ensure this logic works for prices like "1,234.56"
        values.put(ReceiptsTable.COLUMN_PRICE, receipt.getPrice().getPrice().doubleValue());
        values.put(ReceiptsTable.COLUMN_TAX, receipt.getTax().getPrice().doubleValue());
        final BigDecimal exchangeRate = receipt.getPrice().getExchangeRate().getExchangeRate(receipt.getTrip().getDefaultCurrencyCode());
        if (exchangeRate != null) {
            values.put(ReceiptsTable.COLUMN_EXCHANGE_RATE, exchangeRate.doubleValue());
        }

        // Add extras
        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1, receipt.getExtraEditText1());
        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2, receipt.getExtraEditText2());
        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3, receipt.getExtraEditText3());
        if (databaseOperationMetadata.getOperationFamilyType() == OperationFamilyType.Sync) {
            values.putAll(mSyncStateAdapter.write(receipt.getSyncState()));
        } else {
            values.putAll(mSyncStateAdapter.writeUnsynced(receipt.getSyncState()));
        }

        values.put(ReceiptsTable.COLUMN_CUSTOM_ORDER_ID, receipt.getCustomOrderId());

        return values;
    }

    @NonNull
    @Override
    public Receipt build(@NonNull Receipt receipt, @NonNull PrimaryKey<Receipt, Integer> primaryKey, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        return new ReceiptBuilderFactory(primaryKey.getPrimaryKeyValue(receipt), receipt)
                .setSyncState(mSyncStateAdapter.get(receipt.getSyncState(), databaseOperationMetadata)).build();
    }


}

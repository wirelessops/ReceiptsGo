package co.smartreceipts.android.model.factory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.TimeZone;
import java.util.UUID;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.date.DisplayableDate;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Keyed;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.receipts.ordering.ReceiptsOrderer;
import co.smartreceipts.core.sync.model.SyncState;
import co.smartreceipts.core.sync.model.impl.DefaultSyncState;

/**
 * A {@link co.smartreceipts.android.model.Receipt} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.Receipt} objects
 */
public class ReceiptBuilderFactory implements BuilderFactory<Receipt> {

    private Trip trip;
    private PaymentMethod paymentMethod;
    private File file;
    private String name;
    private Category category;
    private String comment;
    private String extraEditText1;
    private String extraEditText2;
    private String extraEditText3;
    private final PriceBuilderFactory priceBuilderFactory, taxBuilderFactory;
    private Date date;
    private TimeZone timeZone;
    private int id;
    private int index;
    private boolean isReimbursable, isFullPage, isSelected;
    private SyncState syncState;
    private long orderId;
    private UUID uuid;

    public ReceiptBuilderFactory() {
        this(Keyed.MISSING_ID);
    }

    public ReceiptBuilderFactory(int id) {
        this.id = id;
        name = "";
        comment = "";
        priceBuilderFactory = new PriceBuilderFactory();
        taxBuilderFactory = new PriceBuilderFactory();
        date = new Date(System.currentTimeMillis());
        timeZone = TimeZone.getDefault();
        index = -1;
        syncState = new DefaultSyncState();
        orderId = ReceiptsOrderer.Companion.getDefaultCustomOrderId(date);
        uuid = Keyed.Companion.getMISSING_UUID();
    }

    public ReceiptBuilderFactory(@NonNull Receipt receipt) {
        id = receipt.getId();
        trip = receipt.getTrip();
        name = receipt.getName();
        file = receipt.getFile();
        priceBuilderFactory = new PriceBuilderFactory().setPrice(receipt.getPrice());
        taxBuilderFactory = new PriceBuilderFactory().setPrice(receipt.getTax());
        date = (Date) receipt.getDate().clone();
        timeZone = receipt.getTimeZone();
        category = receipt.getCategory();
        comment = receipt.getComment();
        paymentMethod = receipt.getPaymentMethod();
        isReimbursable = receipt.isReimbursable();
        isFullPage = receipt.isFullPage();
        isSelected = receipt.isSelected();
        extraEditText1 = receipt.getExtraEditText1();
        extraEditText2 = receipt.getExtraEditText2();
        extraEditText3 = receipt.getExtraEditText3();
        index = receipt.getIndex();
        syncState = receipt.getSyncState();
        orderId = receipt.getCustomOrderId();
        uuid = receipt.getUuid();
    }

    public ReceiptBuilderFactory(int id, @NonNull Receipt receipt) {
        this(receipt);
        this.id = id;
    }

    public ReceiptBuilderFactory setUuid(@NonNull UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public ReceiptBuilderFactory setTrip(@NonNull Trip trip) {
        this.trip = trip;
        return this;
    }

    public ReceiptBuilderFactory setPaymentMethod(@NonNull PaymentMethod method) {
        paymentMethod = method;
        return this;
    }

    public ReceiptBuilderFactory setName(@NonNull String name) {
        this.name = name;
        return this;
    }

    public ReceiptBuilderFactory setCategory(@NonNull Category category) {
        this.category = category;
        return this;
    }

    public ReceiptBuilderFactory setComment(@NonNull String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Sets the price of this ReceiptBuilderFactory as a string (useful for user input)
     *
     * @param price - the desired price as a string
     * @return the {@link ReceiptBuilderFactory} instance for method chaining
     */
    public ReceiptBuilderFactory setPrice(String price) {
        priceBuilderFactory.setPrice(price);
        return this;
    }

    public ReceiptBuilderFactory setPrice(double price) {
        priceBuilderFactory.setPrice(price);
        return this;
    }

    public ReceiptBuilderFactory setPrice(BigDecimal price) {
        priceBuilderFactory.setPrice(price);
        return this;
    }

    public ReceiptBuilderFactory setPrice(Price price) {
        priceBuilderFactory.setPrice(price);
        return this;
    }

    public ReceiptBuilderFactory setExchangeRate(ExchangeRate exchangeRate) {
        priceBuilderFactory.setExchangeRate(exchangeRate);
        taxBuilderFactory.setExchangeRate(exchangeRate);
        return this;
    }

    /**
     * Sets the tax of this ReceiptBuilderFactory as a string (useful for user input)
     *
     * @param tax - the desired tax as a string
     * @return the {@link ReceiptBuilderFactory} instance for method chaining
     */
    public ReceiptBuilderFactory setTax(String tax) {
        taxBuilderFactory.setPrice(tax);
        return this;
    }

    public ReceiptBuilderFactory setTax(double tax) {
        taxBuilderFactory.setPrice(tax);
        return this;
    }

    public ReceiptBuilderFactory setTax(Price tax) {
        taxBuilderFactory.setPrice(tax);
        return this;
    }

    public ReceiptBuilderFactory setFile(File file) {
        this.file = file;
        return this;
    }

    public ReceiptBuilderFactory setDate(Date date) {
        this.date = date;
        return this;
    }

    public ReceiptBuilderFactory setDate(long datetime) {
        date = new Date(datetime);
        return this;
    }

    public ReceiptBuilderFactory setTimeZone(@Nullable String timeZoneId) {
        if (timeZoneId != null) {
            timeZone = TimeZone.getTimeZone(timeZoneId);
        }
        return this;
    }

    public ReceiptBuilderFactory setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    public ReceiptBuilderFactory setIsReimbursable(boolean isReimbursable) {
        this.isReimbursable = isReimbursable;
        return this;
    }

    public ReceiptBuilderFactory setIsFullPage(boolean isFullPage) {
        this.isFullPage = isFullPage;
        return this;
    }

    public ReceiptBuilderFactory setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
        return this;
    }

    public ReceiptBuilderFactory setCurrency(PriceCurrency currency) {
        priceBuilderFactory.setCurrency(currency);
        taxBuilderFactory.setCurrency(currency);
        return this;
    }

    public ReceiptBuilderFactory setCurrency(String currencyCode) {
        priceBuilderFactory.setCurrency(currencyCode);
        taxBuilderFactory.setCurrency(currencyCode);
        return this;
    }

    public ReceiptBuilderFactory setExtraEditText1(String extraEditText1) {
        if (!DatabaseHelper.NO_DATA.equals(extraEditText1)) {
            this.extraEditText1 = extraEditText1;
        } else {
            this.extraEditText1 = null;
        }
        return this;
    }

    public ReceiptBuilderFactory setExtraEditText2(String extraEditText2) {
        if (!DatabaseHelper.NO_DATA.equals(extraEditText2)) {
            this.extraEditText2 = extraEditText2;
        } else {
            this.extraEditText2 = null;
        }
        return this;
    }

    public ReceiptBuilderFactory setExtraEditText3(String extraEditText3) {
        if (!DatabaseHelper.NO_DATA.equals(extraEditText3)) {
            this.extraEditText3 = extraEditText3;
        } else {
            this.extraEditText3 = null;
        }
        return this;
    }

    public ReceiptBuilderFactory setIndex(int index) {
        this.index = index;
        return this;
    }

    public ReceiptBuilderFactory setSyncState(@NonNull SyncState syncState) {
        this.syncState = Preconditions.checkNotNull(syncState);
        return this;
    }

    public ReceiptBuilderFactory setCustomOrderId(long order_id) {
        orderId = order_id;
        return this;
    }

    @Override
    @NonNull
    public Receipt build() {
        final DisplayableDate displayableDate = new DisplayableDate(date, timeZone);
        return new Receipt(id, uuid, index, trip, file,
                paymentMethod == null ? PaymentMethod.Companion.getNONE() : paymentMethod, name,
                category == null ? new CategoryBuilderFactory().build() : category, comment,
                priceBuilderFactory.build(), taxBuilderFactory.build(), displayableDate, isReimbursable,
                isFullPage, isSelected, extraEditText1, extraEditText2, extraEditText3, syncState, orderId);
    }

}

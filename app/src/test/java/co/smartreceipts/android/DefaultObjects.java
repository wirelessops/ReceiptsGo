package co.smartreceipts.android;

import androidx.annotation.NonNull;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Collections;
import java.util.TimeZone;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.date.DisplayableDate;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Keyed;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.impl.ImmutablePriceImpl;
import co.smartreceipts.core.sync.model.SyncState;
import co.smartreceipts.core.sync.model.impl.DefaultSyncState;
import co.smartreceipts.core.sync.model.impl.Identifier;
import co.smartreceipts.core.sync.model.impl.IdentifierMap;
import co.smartreceipts.core.sync.model.impl.MarkedForDeletionMap;
import co.smartreceipts.core.sync.model.impl.SyncStatusMap;
import co.smartreceipts.core.sync.provider.SyncProvider;

public class DefaultObjects {

    private DefaultObjects() {
    }

    @NonNull
    public static Trip newDefaultTrip() {
        return new Trip(Keyed.MISSING_ID, Keyed.Companion.getMISSING_UUID(),
                new File(new File("").getAbsolutePath()),
                new DisplayableDate(new Date(System.currentTimeMillis()), TimeZone.getDefault()),
                new DisplayableDate(new Date(System.currentTimeMillis()), TimeZone.getDefault()),
                PriceCurrency.getDefaultCurrency(), "comment", "costCenter");
    }

    @NonNull
    public static SyncState newDefaultSyncState() {
        return new DefaultSyncState(new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, new Identifier("abc"))),
                new SyncStatusMap(Collections.singletonMap(SyncProvider.GoogleDrive, true)),
                new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, true)),
                new Date(System.currentTimeMillis()));
    }

    @NonNull
    public static PaymentMethod newDefaultPaymentMethod() {
        return new PaymentMethodBuilderFactory().setId(23).setMethod("method").build();
    }

    @NonNull
    public static Category newDefaultCategory() {
        return new CategoryBuilderFactory().setName("name").setCode("code").build();
    }

    @NonNull
    public static Price newDefaultPrice() {
        return new ImmutablePriceImpl(new BigDecimal(5), PriceCurrency.getInstance("USD"), new ExchangeRate("USD", Collections.singletonMap("USD", 1.00d)));
    }

    @NonNull
    public static Price newDefaultTax() {
        return new ImmutablePriceImpl(new BigDecimal(2), PriceCurrency.getInstance("USD"), new ExchangeRate("USD", Collections.singletonMap("USD", 1.00d)));
    }

}

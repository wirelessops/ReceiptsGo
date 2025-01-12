package com.wops.receiptsgo.apis.gson;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

import com.wops.receiptsgo.model.Category;
import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.ColumnDefinitions;
import com.wops.receiptsgo.model.PaymentMethod;
import com.wops.receiptsgo.model.Receipt;

public class SmartReceiptsGsonBuilder {

    private final ColumnDefinitions<Receipt> mReceiptColumnDefinitions;

    public SmartReceiptsGsonBuilder(@NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        mReceiptColumnDefinitions = Preconditions.checkNotNull(receiptColumnDefinitions);
    }

    @NonNull
    public Gson create() {
        final GsonBuilder builder = new GsonBuilder();
        builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        builder.registerTypeAdapter(Date.class, new GsonUtcDateAdapter());
        builder.registerTypeAdapter(Column.class, new ColumnGsonAdapter(mReceiptColumnDefinitions));
        builder.registerTypeAdapter(PaymentMethod.class, new PaymentMethodGsonAdapter());
        builder.registerTypeAdapter(Category.class, new CategoryGsonAdapter());
        return builder.create();
    }
}

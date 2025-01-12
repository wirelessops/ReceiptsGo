package com.wops.receiptsgo.apis.gson;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.ColumnDefinitions;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.factory.ColumnBuilderFactory;

public class ColumnGsonAdapter implements GsonAdapter<Column<Receipt>> {

    private final ColumnDefinitions<Receipt> mReceiptColumnDefinitions;

    public ColumnGsonAdapter(@NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        mReceiptColumnDefinitions = Preconditions.checkNotNull(receiptColumnDefinitions);
    }

    @Override
    public Column<Receipt> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final int type = json.getAsInt();
        return new ColumnBuilderFactory<>(mReceiptColumnDefinitions).setColumnType(type).build();
    }
}

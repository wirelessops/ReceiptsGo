package com.wops.receiptsgo.apis.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import com.wops.receiptsgo.model.Category;
import com.wops.receiptsgo.model.factory.CategoryBuilderFactory;

public class CategoryGsonAdapter implements GsonAdapter<Category> {

    private final String NAME = "Name";
    private final String CODE = "Code";
    private final String CUSTOM_ORDER = "Custom_order_id";

    @Override
    public Category deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        final String name = jsonObject.get(NAME).getAsString();
        final String code = jsonObject.get(CODE).getAsString();
        final int orderId = jsonObject.get(CUSTOM_ORDER).getAsInt();
        return new CategoryBuilderFactory().setName(name).setCode(code).setCustomOrderId(orderId).build();
    }
}

package com.wops.receiptsgo.model.factory;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.UUID;

import com.wops.receiptsgo.model.Category;
import com.wops.receiptsgo.model.Keyed;
import com.wops.receiptsgo.model.PaymentMethod;
import co.smartreceipts.core.sync.model.SyncState;
import co.smartreceipts.core.sync.model.impl.DefaultSyncState;

/**
 * A {@link com.wops.receiptsgo.model.Receipt} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link PaymentMethod} objects
 */
public class CategoryBuilderFactory implements BuilderFactory<Category> {

    private int id;
    private UUID uuid;
    private String name;
    private String code;
    private SyncState syncState;
    private long customOrderId;

    /**
     * Default constructor for this class
     */
    public CategoryBuilderFactory() {
        id = Keyed.MISSING_ID;
        uuid = Keyed.Companion.getMISSING_UUID();
        name = "";
        code = "";
        syncState = new DefaultSyncState();
        customOrderId  = 0;
    }

    public CategoryBuilderFactory(Category category) {
        id = category.getId();
        uuid = category.getUuid();
        name = category.getName();
        code = category.getCode();
        syncState = category.getSyncState();
        customOrderId  = category.getCustomOrderId();
    }


    /**
     * Defines the primary key id for this object
     *
     * @param id - the id
     * @return this {@link CategoryBuilderFactory} for method chaining
     */
    public CategoryBuilderFactory setId(int id) {
        this.id = id;
        return this;
    }

    /**
     * Defines the uuid for this object
     *
     * @param uuid - the uuid
     * @return this {@link CategoryBuilderFactory} for method chaining
     */
    public CategoryBuilderFactory setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * Defines the "name" for this category
     *
     * @param name - the name
     * @return this {@link CategoryBuilderFactory} for method chaining
     */
    public CategoryBuilderFactory setName(@NonNull String name) {
        this.name = Preconditions.checkNotNull(name);
        return this;
    }

    /**
     * Defines the "code" for this category
     *
     * @param code - the category code
     * @return this {@link CategoryBuilderFactory} for method chaining
     */
    public CategoryBuilderFactory setCode(@NonNull String code) {
        this.code = Preconditions.checkNotNull(code);
        return this;
    }

    /**
     * Defines the "custom_order_id" for this category
     *
     * @param orderId - the category custom order id
     * @return this {@link CategoryBuilderFactory} for method chaining
     */
    public CategoryBuilderFactory setCustomOrderId(long orderId) {
        this.customOrderId = orderId;
        return this;
    }

    public CategoryBuilderFactory setSyncState(@NonNull SyncState syncState) {
        this.syncState = Preconditions.checkNotNull(syncState);
        return this;
    }

    /**
     * @return - the {@link Category} object as set by the setter methods in this class
     */
    @NonNull
    public Category build() {
        return new Category(id, uuid, name, code, syncState, customOrderId);
    }
}

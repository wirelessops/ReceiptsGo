package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.impl.ImmutableCategoryImpl;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

/**
 * A {@link co.smartreceipts.android.model.Receipt} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link PaymentMethod} objects
 */
public class CategoryBuilderFactory implements BuilderFactory<Category> {

    private int id;
    private String name;
    private String code;
    private SyncState syncState;

    /**
     * Default constructor for this class
     */
    public CategoryBuilderFactory() {
        id = MISSING_ID;
        name = "";
        code = "";
        syncState = new DefaultSyncState();
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

    public CategoryBuilderFactory setSyncState(@NonNull SyncState syncState) {
        this.syncState = Preconditions.checkNotNull(syncState);
        return this;
    }

    /**
     * @return - the {@link Category} object as set by the setter methods in this class
     */
    @NonNull
    public Category build() {
        return new ImmutableCategoryImpl(id, name, code, syncState);
    }
}

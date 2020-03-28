package co.smartreceipts.android.model.factory;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.UUID;

import co.smartreceipts.android.model.Keyed;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.core.sync.model.SyncState;
import co.smartreceipts.core.sync.model.impl.DefaultSyncState;

/**
 * A {@link co.smartreceipts.android.model.Receipt} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.PaymentMethod} objects
 */
public class PaymentMethodBuilderFactory implements BuilderFactory<PaymentMethod> {

    private int _id;
    private UUID _uuid;
    private String _method;
    private SyncState _syncState;
    private long _customOrderId;

    /**
     * Default constructor for this class
     */
    public PaymentMethodBuilderFactory() {
        _id = Keyed.MISSING_ID;
        _uuid = Keyed.Companion.getMISSING_UUID();
        _method = "";
        _syncState = new DefaultSyncState();
        _customOrderId = 0;
    }

    public PaymentMethodBuilderFactory(final PaymentMethod paymentMethod) {
        _id = paymentMethod.getId();
        _uuid = paymentMethod.getUuid();
        _method = paymentMethod.getMethod();
        _syncState = paymentMethod.getSyncState();
        _customOrderId = paymentMethod.getCustomOrderId();
    }

    /**
     * Defines the primary key id for this object
     *
     * @param id - the id
     * @return this {@link PaymentMethodBuilderFactory} for method chaining
     */
    public PaymentMethodBuilderFactory setId(int id) {
        _id = id;
        return this;
    }

    /**
     * Defines the uuid for this object
     *
     * @param uuid - the uuid
     * @return this {@link PaymentMethodBuilderFactory} for method chaining
     */
    public PaymentMethodBuilderFactory setUuid(UUID uuid) {
        _uuid = uuid;
        return this;
    }

    /**
     * Defines the payment method type for this object
     *
     * @param method - the payment method
     * @return this {@link PaymentMethodBuilderFactory} for method chaining
     */
    public PaymentMethodBuilderFactory setMethod(@NonNull String method) {
        _method = Preconditions.checkNotNull(method);
        return this;
    }

    public PaymentMethodBuilderFactory setSyncState(@NonNull SyncState syncState) {
        _syncState = Preconditions.checkNotNull(syncState);
        return this;
    }


    /**
     * Defines the "custom_order_id" for this payment method
     *
     * @param orderId - the payment method custom order id
     * @return this {@link PaymentMethodBuilderFactory} for method chaining
     */
    public PaymentMethodBuilderFactory setCustomOrderId(long orderId) {
        _customOrderId = orderId;
        return this;
    }

    /**
     * @return - the {@link PaymentMethodBuilderFactory} object as set by the setter methods in this class
     */
    @NonNull
    public PaymentMethod build() {
        return new PaymentMethod(_id, _uuid, _method, _syncState, _customOrderId);
    }
}

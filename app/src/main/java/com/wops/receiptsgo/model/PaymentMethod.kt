package com.wops.receiptsgo.model

import android.content.res.Resources
import android.os.Parcelable
import com.wops.receiptsgo.model.factory.PaymentMethodBuilderFactory
import com.wops.core.sync.model.SyncState
import com.wops.core.sync.model.Syncable
import com.wops.core.sync.model.impl.DefaultSyncState
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * An immutable implementation of [PaymentMethod].
 *
 * @author Will Baumann
 */
@Parcelize
class PaymentMethod @JvmOverloads constructor (
    override val id: Int,
    override val uuid: UUID,
    val method: String, // The actual payment method that the user specified
    val isReimbursable: Boolean,
    override val syncState: SyncState = DefaultSyncState(),
    override val customOrderId: Long = 0

) : Keyed, Parcelable, Syncable, Draggable<PaymentMethod> {

    override fun toString() = method

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PaymentMethod) return false

        val that = other as PaymentMethod?

        if (id != that!!.id) return false
        if (uuid != that.uuid) return false
        if (isReimbursable != that.isReimbursable) return false
        return if (customOrderId != that.customOrderId) false else method == that.method
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + uuid.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + isReimbursable.hashCode()
        result = 31 * result + (customOrderId xor customOrderId.ushr(32)).toInt()
        return result
    }

    override fun compareTo(other: PaymentMethod): Int {
        return customOrderId.compareTo(other.customOrderId)
    }

    companion object {
        val NONE = PaymentMethodBuilderFactory().setMethod(Resources.getSystem().getString(android.R.string.untitled)).build()

        @JvmField val PARCEL_KEY: String = PaymentMethod::class.java.name
    }
}

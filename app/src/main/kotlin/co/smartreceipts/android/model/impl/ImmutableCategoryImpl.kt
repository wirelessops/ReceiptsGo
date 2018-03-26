package co.smartreceipts.android.model.impl

import co.smartreceipts.android.model.Category
import co.smartreceipts.android.sync.model.SyncState
import co.smartreceipts.android.sync.model.impl.DefaultSyncState
import kotlinx.android.parcel.Parcelize

@Parcelize
class ImmutableCategoryImpl @JvmOverloads constructor(
    override val id: Int,
    override val name: String,
    override val code: String,
    override val syncState: SyncState = DefaultSyncState(),
    override val customOrderId: Long = id.toLong()
) : Category {

    override fun toString() = name

    override fun compareTo(other: Category): Int {
        return if (customOrderId == 0L && other.customOrderId == 0L) {
            name.compareTo(other.name)
        } else {
            customOrderId.compareTo( other.customOrderId)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImmutableCategoryImpl

        if (id != other.id) return false
        if (name != other.name) return false
        if (code != other.code) return false
        if (customOrderId != other.customOrderId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + customOrderId.hashCode()
        return result
    }


}

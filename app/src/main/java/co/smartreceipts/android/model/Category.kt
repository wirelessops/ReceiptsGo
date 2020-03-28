package co.smartreceipts.android.model

import android.os.Parcelable
import co.smartreceipts.core.sync.model.SyncState
import co.smartreceipts.core.sync.model.Syncable
import co.smartreceipts.core.sync.model.impl.DefaultSyncState
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class Category @JvmOverloads constructor(
    override val id: Int,
    override val uuid: UUID,
    val name: String, // The full-name representation of this category
    val code: String, // The "code" associated with this category
    override val syncState: SyncState = DefaultSyncState(),
    override val customOrderId: Long = id.toLong()
) : Keyed, Parcelable, Syncable, Draggable<Category> {

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

        other as Category

        if (id != other.id) return false
        if (uuid != other.uuid) return false
        if (name != other.name) return false
        if (code != other.code) return false
        if (customOrderId != other.customOrderId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + uuid.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + customOrderId.hashCode()
        return result
    }

    companion object {
        @JvmField val PARCEL_KEY: String = Category::class.java.name
    }

}

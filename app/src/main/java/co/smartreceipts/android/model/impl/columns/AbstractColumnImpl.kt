package co.smartreceipts.android.model.impl.columns

import co.smartreceipts.android.model.ActualColumnDefinition
import co.smartreceipts.android.model.Column
import co.smartreceipts.android.model.Keyed
import co.smartreceipts.core.sync.model.SyncState
import java.util.*

/**
 * Provides an abstract implementation of the column contract to cover shared code
 */
abstract class AbstractColumnImpl<T> @JvmOverloads constructor(
    override val id: Int,
    private val columnDefinition: ActualColumnDefinition,
    override val syncState: SyncState,
    override val customOrderId: Long = 0,
    override val uuid: UUID = Keyed.MISSING_UUID
) : Column<T> {

    override val type: Int = columnDefinition.columnType

    override val headerStringResId = columnDefinition.columnHeaderId

    override fun getFooter(rows: List<T>) = ""

    override operator fun compareTo(other: Column<T>): Int =
        if (customOrderId == other.customOrderId) {
            id - other.id
        }
        else java.lang.Long.compare(customOrderId, other.customOrderId)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as AbstractColumnImpl<*>?
        return id == that!!.id &&
                uuid == that.uuid &&
                customOrderId == that.customOrderId &&
                columnDefinition == that.columnDefinition
    }

    override fun hashCode(): Int = Objects.hash(id, uuid, columnDefinition, customOrderId)
}

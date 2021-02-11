package co.smartreceipts.android.adapters

import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.search.delegates.SelectedItemIdHolder
import co.smartreceipts.android.search.delegates.tripAdapterDelegate
import co.smartreceipts.android.sync.BackupProvidersManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter

class TripAdapter(
    tripClickListener: (Trip) -> Unit,
    tripLongClickListener: (Trip) -> Unit,
    dateFormatter: DateFormatter,
    backupProvidersManager: BackupProvidersManager
) :
    ListDelegationAdapter<List<Any>>(),
    SelectedItemIdHolder {

    override var selectedItemId: Int = -1
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    init {
        delegatesManager.addDelegate(
            tripAdapterDelegate(
                tripClickListener,
                tripLongClickListener,
                dateFormatter,
                backupProvidersManager,
                this
            )
        )
    }

    override fun getItems() : ArrayList<Trip> {
        return ArrayList(items.filterIsInstance<Trip>())
    }
}
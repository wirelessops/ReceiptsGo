package com.wops.receiptsgo.adapters

import com.wops.receiptsgo.date.DateFormatter
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.search.delegates.SelectedItemIdHolder
import com.wops.receiptsgo.search.delegates.tripAdapterDelegate
import com.wops.receiptsgo.sync.BackupProvidersManager
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
        items = mutableListOf()

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

    override fun getItems() : List<Trip> {
        return items.filterIsInstance<Trip>()
    }
}
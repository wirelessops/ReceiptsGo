package co.smartreceipts.android.search

import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.search.delegates.headerAdapterDelegate
import co.smartreceipts.android.search.delegates.receiptAdapterDelegate
import co.smartreceipts.android.search.delegates.tripAdapterDelegate
import co.smartreceipts.core.sync.provider.SyncProvider
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter

class SearchResultsDelegationAdapter(
    tripClickListener: (Trip) -> Unit,
    receiptClickListener: (Receipt) -> Unit,
    dateFormatter: DateFormatter,
    syncProvider: SyncProvider
) : AsyncListDifferDelegationAdapter<Any>(
    SearchResultsDiffUtilCallback(),
    headerAdapterDelegate(),
    tripAdapterDelegate(tripClickListener, {}, dateFormatter),
    receiptAdapterDelegate(receiptClickListener, syncProvider)
)
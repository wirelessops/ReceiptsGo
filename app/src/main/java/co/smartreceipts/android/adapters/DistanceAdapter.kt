package co.smartreceipts.android.adapters

import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.search.delegates.distanceAdapterDelegate
import co.smartreceipts.android.search.delegates.doubleHeaderAdapterDelegate
import co.smartreceipts.android.sync.BackupProvidersManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter

class DistanceAdapter(distanceClickListener: (Distance) -> Unit, backupProvidersManager: BackupProvidersManager) :
    ListDelegationAdapter<List<Any>>(
        doubleHeaderAdapterDelegate(),
        distanceAdapterDelegate(distanceClickListener, backupProvidersManager)
    )

interface DistanceListItem
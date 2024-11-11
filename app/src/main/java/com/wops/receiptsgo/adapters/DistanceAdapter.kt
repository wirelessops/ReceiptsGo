package com.wops.receiptsgo.adapters

import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.search.delegates.distanceAdapterDelegate
import com.wops.receiptsgo.search.delegates.doubleHeaderAdapterDelegate
import com.wops.receiptsgo.sync.BackupProvidersManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter

class DistanceAdapter(distanceClickListener: (Distance) -> Unit, backupProvidersManager: BackupProvidersManager) :
    ListDelegationAdapter<List<Any>>(
        doubleHeaderAdapterDelegate(),
        distanceAdapterDelegate(distanceClickListener, backupProvidersManager)
    )

interface DistanceListItem
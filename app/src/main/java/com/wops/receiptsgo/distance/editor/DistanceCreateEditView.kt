package com.wops.receiptsgo.distance.editor

import com.wops.receiptsgo.autocomplete.AutoCompleteView
import com.wops.receiptsgo.editor.Editor
import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.widget.model.UiIndicator
import io.reactivex.Observable

interface DistanceCreateEditView : Editor<Distance>, AutoCompleteView<Distance> {

    val createDistanceClicks: Observable<Distance>

    val updateDistanceClicks: Observable<Distance>

    val deleteDistanceClicks: Observable<Distance>


    fun present(uiIndicator: UiIndicator<Int>)
}
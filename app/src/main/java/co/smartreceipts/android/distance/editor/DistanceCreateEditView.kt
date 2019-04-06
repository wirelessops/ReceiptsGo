package co.smartreceipts.android.distance.editor

import co.smartreceipts.android.autocomplete.AutoCompleteView
import co.smartreceipts.android.editor.Editor
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.widget.model.UiIndicator
import io.reactivex.Observable

interface DistanceCreateEditView : Editor<Distance>, AutoCompleteView<Distance> {

    val createDistanceClicks: Observable<Distance>

    val updateDistanceClicks: Observable<Distance>

    val deleteDistanceClicks: Observable<Distance>


    fun present(uiIndicator: UiIndicator<Int>)
}
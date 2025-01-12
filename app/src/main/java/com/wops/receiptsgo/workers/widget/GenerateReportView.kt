package com.wops.receiptsgo.workers.widget

import android.app.Activity
import com.wops.receiptsgo.workers.EmailAssistant
import io.reactivex.Observable
import java.util.EnumSet

interface GenerateReportView {

    val generateReportClicks: Observable<EnumSet<EmailAssistant.EmailOptions>>

    val reportSharedEvents: Observable<Unit>

    val getActivity: Activity

    fun present(result: EmailResult)
}
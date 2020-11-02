package co.smartreceipts.android.workers.widget

import co.smartreceipts.android.workers.EmailAssistant
import io.reactivex.Observable
import java.util.*

interface GenerateReportView {

    val generateReportClicks: Observable<EnumSet<EmailAssistant.EmailOptions>>


    fun present(result: EmailResult)
}
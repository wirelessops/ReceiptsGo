package co.smartreceipts.android.workers.widget

import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.workers.EmailAssistant
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import java.util.*

class GenerateReportInteractorTest {

    // Class under test
    private lateinit var interactor: GenerateReportInteractor

    private val emailAssistant = mock<EmailAssistant>()
    private val preferenceManager = mock<UserPreferenceManager>()
    private val trip = mock<Trip>()

    @Before
    fun setUp() {
        interactor = GenerateReportInteractor(emailAssistant, preferenceManager, Schedulers.trampoline(), Schedulers.trampoline())
    }

    @Test
    fun isLandscapeReportEnabledTest() {
        whenever(preferenceManager.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)).thenReturn(true)

        assert(interactor.isLandscapeReportEnabled())
    }

    @Test
    fun generateReportTest() {
        val options: EnumSet<EmailAssistant.EmailOptions> =
            EnumSet.of(EmailAssistant.EmailOptions.PDF_FULL, EmailAssistant.EmailOptions.ZIP)
        val success = EmailResult.Success(mock())

        whenever(emailAssistant.emailTrip(trip, options)).thenReturn(Single.just(success))

        interactor.generateReport(trip, options)
            .test()
            .assertComplete()
            .assertResult(success)

    }
}
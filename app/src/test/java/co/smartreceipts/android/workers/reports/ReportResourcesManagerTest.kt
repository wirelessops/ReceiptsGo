package co.smartreceipts.android.workers.reports

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import wb.android.flex.Flex
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24])
class ReportResourcesManagerTest {

    companion object {
        private const val EN = "en"
        private val EN_US = Locale.US
        private val EN_UK = Locale.UK
        private val DE_DE = Locale.GERMANY
    }

    private lateinit var reportResourcesManager: ReportResourcesManager

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var localizedContext: Context

    @Mock
    private lateinit var resources: Resources

    private val configuration = Configuration()

    @Mock
    private lateinit var preferenceManager: UserPreferenceManager

    @Mock
    private lateinit var flex: Flex

    @Captor
    private lateinit var configurationCaptor: ArgumentCaptor<Configuration>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(context.resources).thenReturn(resources)
        whenever(resources.configuration).thenReturn(configuration)
        whenever(preferenceManager[UserPreference.ReportOutput.PreferredReportLanguage]).thenReturn(EN)
        setDefaultLocaleForConfiguration(EN_US)
        whenever(context.createConfigurationContext(any())).thenReturn(localizedContext)
        reportResourcesManager = ReportResourcesManager(context, preferenceManager, flex)
    }

    @Test
    fun getLocalizedContextIsTheOriginalContextWhenTheLocaleIsTheSameAsTheLanguageForUS() {
        val result = reportResourcesManager.getLocalizedContext()
        verify(context, never()).createConfigurationContext(any())
        assertEquals(result, context)
    }

    @Test
    fun getLocalizedContextIsTheOriginalContextWhenTheLocaleIsTheSameAsTheLanguageForUK() {
        setDefaultLocaleForConfiguration(EN_UK)
        val result = reportResourcesManager.getLocalizedContext()
        verify(context, never()).createConfigurationContext(any())
        assertEquals(result, context)
    }

    @Test
    fun getLocalizedContextIsADifferentContextWhenTheLocaleHasADifferentLanguage() {
        setDefaultLocaleForConfiguration(DE_DE)
        val result = reportResourcesManager.getLocalizedContext()
        verify(context).createConfigurationContext(configurationCaptor.capture())
        assertEquals("en", configurationCaptor.value.locales.toLanguageTags())
        assertEquals(result, localizedContext)
    }

    private fun setDefaultLocaleForConfiguration(locale: Locale) {
        configuration.locales = LocaleList(locale)
    }
}
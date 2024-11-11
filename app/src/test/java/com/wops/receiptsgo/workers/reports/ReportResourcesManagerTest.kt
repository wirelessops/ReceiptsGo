package com.wops.receiptsgo.workers.reports

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import com.wops.receiptsgo.R
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.nhaarman.mockitokotlin2.*
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

    @Test
    fun getCorrectedStringsForTaxColumnNames() {
        whenever(preferenceManager[UserPreference.Receipts.Tax1Name]).thenReturn("tax1 custom name")
        whenever(preferenceManager[UserPreference.Receipts.Tax2Name]).thenReturn("tax2 custom name")

        whenever(flex.getString(any(), eq(R.string.pref_receipt_tax1_name_defaultValue))).thenReturn("tax1")
        whenever(flex.getString(any(), eq(R.string.pref_receipt_tax2_name_defaultValue))).thenReturn("tax2")

        val tax1 = reportResourcesManager.getFlexString(R.string.pref_receipt_tax1_name_defaultValue)
        val tax2 = reportResourcesManager.getFlexString(R.string.pref_receipt_tax2_name_defaultValue)

        assertEquals("tax1 custom name", tax1)
        assertEquals("tax2 custom name", tax2)

        verifyZeroInteractions(flex)
    }

    private fun setDefaultLocaleForConfiguration(locale: Locale) {
        configuration.setLocales(LocaleList(locale))
    }
}
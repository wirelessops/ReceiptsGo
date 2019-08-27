package co.smartreceipts.android.date

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.utils.TestLocaleToggler
import co.smartreceipts.android.utils.TestTimezoneToggler
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.sql.Date
import java.util.*

/**
 * Note: Be careful to generally avoid testing the results of [DateFormatter.DateFormatOption.Default],
 * since this can differ based on the system OS
 */
@RunWith(RobolectricTestRunner::class)
class DateFormatterTest {

    companion object {
        /**
         * A timestamp for a date that occurs on January 31, 2019 in the America/New_York time ozne
         */
        private const val TIMESTAMP = 1548955843313

        private const val DEFAULT_DATE_SEPARATOR = "/"
        private val DEFAULT_DATE_FORMAT_STRING = ApplicationProvider.getApplicationContext<Context>().getString(DateFormatter.DateFormatOption.yyyy_MM_dd.stringResId)
        private val TIMEZONE = TimeZone.getTimeZone("America/New_York")
    }

    private lateinit var dateFormatter: DateFormatter

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Mock
    private lateinit var userPreferenceManager: UserPreferenceManager

    private val scheduler = Schedulers.trampoline()

    private val userPreferenceChangeStream = PublishSubject.create<UserPreference<*>>()

    @Before
    fun setUp() {
        TestLocaleToggler.setDefaultLocale(Locale.US)
        TestTimezoneToggler.setDefaultTimeZone(TIMEZONE)

        MockitoAnnotations.initMocks(this)
        whenever(userPreferenceManager.userPreferenceChangeStream).thenReturn(userPreferenceChangeStream)
        whenever(userPreferenceManager.getSingle(UserPreference.General.DateSeparator)).thenReturn(Single.just(DEFAULT_DATE_SEPARATOR))
        whenever(userPreferenceManager.getSingle(UserPreference.General.DateFormat)).thenReturn(Single.just(DEFAULT_DATE_FORMAT_STRING))
        dateFormatter = DateFormatter(context, userPreferenceManager, scheduler)
        dateFormatter.initialize()
    }

    @After
    fun tearDown() {
        TestLocaleToggler.resetDefaultLocale()
        TestTimezoneToggler.resetDefaultTimeZone()
    }

    @Test
    fun getFormattedDateForEachOption() {
        val displayableDate = DisplayableDate(Date(TIMESTAMP), TIMEZONE)
        assertEquals("1/31/2019", dateFormatter.getFormattedDate(displayableDate, DateFormatter.DateFormatOption.M_d_yyyy))
        assertEquals("31/1/2019", dateFormatter.getFormattedDate(displayableDate, DateFormatter.DateFormatOption.d_M_yyyy))
        assertEquals("2019/01/31", dateFormatter.getFormattedDate(displayableDate, DateFormatter.DateFormatOption.yyyy_MM_dd))
        // For default, just verify that it includes our date separator
        assertTrue(dateFormatter.getFormattedDate(displayableDate, DateFormatter.DateFormatOption.Default).contains("/"))
    }

    @Test
    fun getFormattedDateForDisplayableDate() {
        val displayableDate = DisplayableDate(Date(TIMESTAMP), TIMEZONE)
        assertEquals("2019/01/31", dateFormatter.getFormattedDate(displayableDate))
    }

    @Test
    fun getFormattedDateForSqlDate() {
        assertEquals("2019/01/31", dateFormatter.getFormattedDate(Date(TIMESTAMP), TIMEZONE))
    }

    @Test
    fun getFormattedDateForJavaDate() {
        assertEquals("2019/01/31", dateFormatter.getFormattedDate(java.util.Date(TIMESTAMP), TIMEZONE))
    }

    @Test
    fun getFormattedDateWhenChangingFormat() {
        // We test once beforehand to ensure that there are no issues with our internal caching
        val displayableDate = DisplayableDate(Date(TIMESTAMP), TIMEZONE)
        assertEquals("2019/01/31", dateFormatter.getFormattedDate(java.util.Date(TIMESTAMP), TIMEZONE))

        // Now change the format
        whenever(userPreferenceManager[UserPreference.General.DateFormat]).thenReturn(ApplicationProvider.getApplicationContext<Context>().getString(DateFormatter.DateFormatOption.M_d_yyyy.stringResId))
        userPreferenceChangeStream.onNext(UserPreference.General.DateFormat)
        assertEquals("1/31/2019", dateFormatter.getFormattedDate(displayableDate))
    }

    @Test
    fun getFormattedDateWhenChangingSeparator() {
        // We test once beforehand to ensure that there are no issues with our internal caching
        val displayableDate = DisplayableDate(Date(TIMESTAMP), TIMEZONE)
        assertEquals("2019/01/31", dateFormatter.getFormattedDate(java.util.Date(TIMESTAMP), TIMEZONE))

        // Now change the format
        whenever(userPreferenceManager[UserPreference.General.DateSeparator]).thenReturn("--")
        userPreferenceChangeStream.onNext(UserPreference.General.DateSeparator)
        assertEquals("2019--01--31", dateFormatter.getFormattedDate(displayableDate))
    }

    @Test
    fun getFormattedDateWhenChangingFormatAndSeparator() {
        // We test once beforehand to ensure that there are no issues with our internal caching
        val displayableDate = DisplayableDate(Date(TIMESTAMP), TIMEZONE)
        assertEquals("2019/01/31", dateFormatter.getFormattedDate(java.util.Date(TIMESTAMP), TIMEZONE))

        // Now change the format
        whenever(userPreferenceManager[UserPreference.General.DateFormat]).thenReturn(ApplicationProvider.getApplicationContext<Context>().getString(DateFormatter.DateFormatOption.M_d_yyyy.stringResId))
        userPreferenceChangeStream.onNext(UserPreference.General.DateFormat)
        whenever(userPreferenceManager[UserPreference.General.DateSeparator]).thenReturn("--")
        userPreferenceChangeStream.onNext(UserPreference.General.DateSeparator)
        assertEquals("1--31--2019", dateFormatter.getFormattedDate(displayableDate))
    }
}
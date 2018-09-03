package co.smartreceipts.android.settings

import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.TypedValue
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.utils.TestUtils
import com.nhaarman.mockito_kotlin.whenever
import dagger.Lazy
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.*

@RunWith(RobolectricTestRunner::class)
class UserPreferenceManagerTest {

    // Class under test
    private lateinit var userPreferenceManager: UserPreferenceManager

    private lateinit var preferences: SharedPreferences

    private lateinit var defaultLocale: Locale

    @Mock
    private lateinit var sharedPreferencesLazy: Lazy<SharedPreferences>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        defaultLocale = Locale.getDefault()
        preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
        whenever(sharedPreferencesLazy.get()).thenReturn(preferences)
        userPreferenceManager = UserPreferenceManager(RuntimeEnvironment.application, sharedPreferencesLazy, Schedulers.trampoline())
    }

    @After
    fun tearDown() {
        Locale.setDefault(defaultLocale)
        preferences.edit().clear().apply()
    }

    @Test
    fun initialize() {
        userPreferenceManager.initialize()

        // Just confirm that we properly apply the default values for these special cases
        assertTrue(preferences.contains(RuntimeEnvironment.application.getString(UserPreference.General.DateSeparator.name)))
        assertTrue(preferences.contains(RuntimeEnvironment.application.getString(UserPreference.General.DefaultCurrency.name)))
        assertEquals("USD", preferences.getString(RuntimeEnvironment.application.getString(UserPreference.General.DefaultCurrency.name), null))
        assertTrue(preferences.contains(RuntimeEnvironment.application.getString(UserPreference.Receipts.MinimumReceiptPrice.name)))
        assertEquals((-java.lang.Float.MAX_VALUE).toDouble(), preferences.getFloat(RuntimeEnvironment.application.getString(UserPreference.Receipts.MinimumReceiptPrice.name), 0f).toDouble(), TestUtils.EPSILON.toDouble())
        assertTrue(preferences.contains(RuntimeEnvironment.application.getString(UserPreference.ReportOutput.PreferredReportLanguage.name)))
    }

    @Test
    fun initializeForBadLocale() {
        Locale.setDefault(Locale(""))
        userPreferenceManager.initialize()

        // Just confirm that we properly apply the default values for these special cases
        assertTrue(preferences.contains(RuntimeEnvironment.application.getString(UserPreference.General.DateSeparator.name)))
        assertTrue(preferences.contains(RuntimeEnvironment.application.getString(UserPreference.General.DefaultCurrency.name)))
        assertEquals("USD", preferences.getString(RuntimeEnvironment.application.getString(UserPreference.General.DefaultCurrency.name), null))
        assertTrue(preferences.contains(RuntimeEnvironment.application.getString(UserPreference.Receipts.MinimumReceiptPrice.name)))
        assertEquals((-java.lang.Float.MAX_VALUE).toDouble(), preferences.getFloat(RuntimeEnvironment.application.getString(UserPreference.Receipts.MinimumReceiptPrice.name), 0f).toDouble(), TestUtils.EPSILON.toDouble())

        // Assert that we haven't set the locale (we'll use the default instead)
        assertFalse(preferences.contains(RuntimeEnvironment.application.getString(UserPreference.ReportOutput.PreferredReportLanguage.name)))
    }

    @Test
    fun getInteger() {
        val intPreference = UserPreference.General.DefaultReportDuration
        val intVal = RuntimeEnvironment.application.resources.getInteger(intPreference.defaultValue)
        assertEquals(intVal.toLong(), userPreferenceManager[intPreference].toLong())

        userPreferenceManager.getObservable(intPreference)
                .test()
                .assertValue(intVal)
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun getBoolean() {
        val booleanPreference = UserPreference.Receipts.UsePaymentMethods
        val boolVal = RuntimeEnvironment.application.resources.getBoolean(booleanPreference.defaultValue)
        assertEquals(boolVal, userPreferenceManager[booleanPreference])

        userPreferenceManager.getObservable(booleanPreference)
                .test()
                .assertValue(boolVal)
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun getString() {
        val stringPreference = UserPreference.PlusSubscription.PdfFooterString
        val stringVal = RuntimeEnvironment.application.getString(stringPreference.defaultValue)
        assertEquals(stringVal, userPreferenceManager[stringPreference])

        userPreferenceManager.getObservable(stringPreference)
                .test()
                .assertValue(stringVal)
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun getFloat() {
        val floatPreference = UserPreference.Receipts.DefaultTaxPercentage
        val typedValue = TypedValue()
        RuntimeEnvironment.application.resources.getValue(floatPreference.defaultValue, typedValue, true)
        val floatVal = typedValue.float

        assertEquals(floatVal.toDouble(), userPreferenceManager[floatPreference].toDouble(), TestUtils.EPSILON.toDouble())

        userPreferenceManager.getObservable(floatPreference)
                .test()
                .assertValue(floatVal)
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun setInteger() {
        val intPreference = UserPreference.General.DefaultReportDuration
        val intVal = 999

        userPreferenceManager[intPreference] = 999
        assertEquals(intVal.toLong(), userPreferenceManager[intPreference].toLong())

        userPreferenceManager.setObservable(intPreference, intVal)
                .test()
                .assertValue(intVal)
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun setBoolean() {
        val booleanPreference = UserPreference.Receipts.UsePaymentMethods
        val boolVal = true

        userPreferenceManager[booleanPreference] = boolVal
        assertEquals(boolVal, userPreferenceManager[booleanPreference])

        userPreferenceManager.setObservable(booleanPreference, boolVal)
                .test()
                .assertValue(boolVal)
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun setString() {
        val stringPreference = UserPreference.PlusSubscription.PdfFooterString
        val stringVal = "test"

        userPreferenceManager[stringPreference] = stringVal
        assertEquals(stringVal, userPreferenceManager[stringPreference])

        userPreferenceManager.setObservable(stringPreference, stringVal)
                .test()
                .assertValue(stringVal)
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun setFloat() {
        val floatPreference = UserPreference.Receipts.DefaultTaxPercentage
        val floatVal = 55.5f

        userPreferenceManager[floatPreference] = floatVal
        assertEquals(floatVal.toDouble(), userPreferenceManager[floatPreference].toDouble(), TestUtils.EPSILON.toDouble())

        userPreferenceManager.setObservable(floatPreference, floatVal)
                .test()
                .assertValue(floatVal)
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun preferenceChangesAreEmitted() {
        // We have to initialize first
        userPreferenceManager.initialize()

        val booleanPreference = UserPreference.Receipts.UsePaymentMethods
        val boolVal = true

        val testObserver = userPreferenceManager.userPreferenceChangeStream
                .test()

        userPreferenceManager[booleanPreference] = boolVal
        assertEquals(boolVal, userPreferenceManager[booleanPreference])

        testObserver.assertValue(UserPreference.Receipts.UsePaymentMethods)
        testObserver.assertNotComplete()
        testObserver.assertNoErrors()
    }

}
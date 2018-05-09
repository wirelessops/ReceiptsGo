package co.smartreceipts.android.analytics

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

import co.smartreceipts.android.SameThreadExecutorService
import co.smartreceipts.android.analytics.events.Event
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference

import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@RunWith(RobolectricTestRunner::class)
class AnalyticsManagerTest {

    internal lateinit var analyticsManager: AnalyticsManager

    @Mock
    internal lateinit var analytics: Analytics

    @Mock
    internal lateinit var userPreferenceManager: UserPreferenceManager

    @Mock
    internal lateinit var event: Event

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        analyticsManager = AnalyticsManager(listOf<Analytics>(analytics), userPreferenceManager, SameThreadExecutorService())
    }

    @Test
    fun recordWhenEnabled() {
        `when`(userPreferenceManager.get(UserPreference.Privacy.EnableAnalytics)).thenReturn(true)
        analyticsManager.record(event)
        verify<Analytics>(analytics).record(event)
    }

    @Test
    fun recordWhenDisabled() {
        `when`(userPreferenceManager.get(UserPreference.Privacy.EnableAnalytics)).thenReturn(false)
        analyticsManager.record(event)
        verify<Analytics>(analytics, never()).record(event)
    }

}
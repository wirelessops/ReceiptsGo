package com.wops.receiptsgo.fragments

import androidx.fragment.app.FragmentActivity
import com.wops.receiptsgo.model.factory.CategoryBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.LooperMode
import java.sql.Date

@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.LEGACY)
class ReceiptInputCacheTest {

    private lateinit var activityController: ActivityController<FragmentActivity>

    @Before
    fun setUp() {
        activityController =
                Robolectric.buildActivity(FragmentActivity::class.java).create(null).start()
                    .resume().visible()
    }

    @Test
    fun getCachedDate() {
        val date = Date(1000000000L)
        val preConfigurationChangeCache =
            ReceiptInputCache(activityController.get().supportFragmentManager)
        preConfigurationChangeCache.cachedDate = date

        activityController.restart()

        val postConfigurationChangeCache =
            ReceiptInputCache(activityController.get().supportFragmentManager)
        assertNotNull(postConfigurationChangeCache.cachedDate)

        // Confirm we bump the time by one for ordering, so cached dates never lead to stale orders
        assertEquals(Date(date.time + 1), postConfigurationChangeCache.cachedDate)
    }

    @Test
    fun getCachedCategory() {
        val category = CategoryBuilderFactory().setName("abc").setCode("def").build()
        val preConfigurationChangeCache =
            ReceiptInputCache(activityController.get().supportFragmentManager)
        preConfigurationChangeCache.cachedCategory = category

        activityController.restart()

        val postConfigurationChangeCache =
            ReceiptInputCache(activityController.get().supportFragmentManager)
        assertEquals(category, postConfigurationChangeCache.cachedCategory)
    }

    @Test
    fun getCachedCurrency() {
        val currency = "USD"
        val preConfigurationChangeCache =
            ReceiptInputCache(activityController.get().supportFragmentManager)
        preConfigurationChangeCache.cachedCurrency = currency

        activityController.restart()

        val postConfigurationChangeCache =
            ReceiptInputCache(activityController.get().supportFragmentManager)
        assertEquals(currency, postConfigurationChangeCache.cachedCurrency)
    }
}
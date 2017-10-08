package co.smartreceipts.android.fragments;

import android.support.v4.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import java.sql.Date;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class ReceiptInputCacheTest {

    ActivityController<FragmentActivity> mActivityController;

    @Before
    public void setUp() throws Exception {
        mActivityController = Robolectric.buildActivity(FragmentActivity.class).create(null).start().resume().visible();
    }

    @Test
    public void getCachedDate() {
        final Date date = new Date(1000000000L);
        final ReceiptInputCache preConfigurationChangeCache = new ReceiptInputCache(mActivityController.get().getSupportFragmentManager());
        preConfigurationChangeCache.setCachedDate(date);

        mActivityController.restart();

        final ReceiptInputCache postConfigurationChangeCache = new ReceiptInputCache(mActivityController.get().getSupportFragmentManager());
        assertNotNull(postConfigurationChangeCache.getCachedDate());

        // Confirm we bump the time by one for ordering
        assertEquals(new Date(date.getTime() + 1), postConfigurationChangeCache.getCachedDate());
    }

    @Test
    public void getCachedCategory() {
        final Category category = new CategoryBuilderFactory().setName("abc").setCode("def").build();
        final ReceiptInputCache preConfigurationChangeCache = new ReceiptInputCache(mActivityController.get().getSupportFragmentManager());
        preConfigurationChangeCache.setCachedCategory(category);

        mActivityController.restart();

        final ReceiptInputCache postConfigurationChangeCache = new ReceiptInputCache(mActivityController.get().getSupportFragmentManager());
        assertEquals(category, postConfigurationChangeCache.getCachedCategory());
    }

    @Test
    public void getCachedCurrency() {
        final String currency = "USD";
        final ReceiptInputCache preConfigurationChangeCache = new ReceiptInputCache(mActivityController.get().getSupportFragmentManager());
        preConfigurationChangeCache.setCachedCurrency(currency);

        mActivityController.restart();

        final ReceiptInputCache postConfigurationChangeCache = new ReceiptInputCache(mActivityController.get().getSupportFragmentManager());
        assertEquals(currency, postConfigurationChangeCache.getCachedCurrency());
    }
}
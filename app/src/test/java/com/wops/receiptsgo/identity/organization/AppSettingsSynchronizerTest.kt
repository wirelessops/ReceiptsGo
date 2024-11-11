package com.wops.receiptsgo.identity.organization

import com.wops.receiptsgo.identity.apis.organizations.AppSettings
import com.wops.receiptsgo.identity.apis.organizations.Configurations
import com.wops.receiptsgo.model.Column
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.factory.CategoryBuilderFactory
import com.wops.receiptsgo.model.factory.PaymentMethodBuilderFactory
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptCommentColumn
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptNameColumn
import com.wops.receiptsgo.persistence.database.controllers.impl.CSVTableController
import com.wops.receiptsgo.persistence.database.controllers.impl.CategoriesTableController
import com.wops.receiptsgo.persistence.database.controllers.impl.PDFTableController
import com.wops.receiptsgo.persistence.database.controllers.impl.PaymentMethodsTableController
import com.wops.core.sync.model.impl.DefaultSyncState
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class AppSettingsSynchronizerTest {

    // Class under test
    private lateinit var appSettingsSynchronizer: AppSettingsSynchronizer


    private val categoriesTableController = mock<CategoriesTableController>()
    private val paymentMethodsTableController = mock<PaymentMethodsTableController>()
    private val csvTableController = mock<CSVTableController>()
    private val pdfTableController = mock<PDFTableController>()
    private val preferencesSynchronizer = mock<AppPreferencesSynchronizer>()

    private val uuid1 = UUID.randomUUID()
    private val uuid2 = UUID.randomUUID()
    private val category1 = CategoryBuilderFactory().setUuid(uuid1).setCode("CODE_1").build()
    private val category2 = CategoryBuilderFactory().setUuid(uuid2).setCode("CODE_2").build()
    private val paymentMethod1 = PaymentMethodBuilderFactory().setUuid(uuid1).setMethod("METHOD_1").build()
    private val paymentMethod2 = PaymentMethodBuilderFactory().setUuid(uuid2).setMethod("METHOD_2").build()
    private val column1 = ReceiptNameColumn(0, DefaultSyncState(), 0, uuid1)
    private val column2 = ReceiptCommentColumn(5, DefaultSyncState(), 0, uuid2)
    private val column3 = ReceiptNameColumn(0, DefaultSyncState(), 0, UUID.randomUUID())

    @Before
    fun setUp() {
        appSettingsSynchronizer =
            AppSettingsSynchronizer(
                categoriesTableController, paymentMethodsTableController, csvTableController, pdfTableController, preferencesSynchronizer
            )

        whenever(categoriesTableController.get()).thenReturn(Single.just(arrayListOf(category1, category2)))
        whenever(paymentMethodsTableController.get()).thenReturn(Single.just(arrayListOf(paymentMethod1, paymentMethod2)))
        whenever(csvTableController.get()).thenReturn(Single.just(arrayListOf(column1, column2) as List<Column<Receipt>>?))
        whenever(pdfTableController.get()).thenReturn(Single.just(arrayListOf(column1, column2) as List<Column<Receipt>>?))
    }

    @Test
    fun getCurrentAppSettingsTest() {
        val prefsMap = emptyMap<String, Any>()
        whenever(preferencesSynchronizer.getAppPreferences()).thenReturn(Single.just(prefsMap))

        val appSettings = AppSettings(
            Configurations(), prefsMap, arrayListOf(category1, category2),
            arrayListOf(paymentMethod1, paymentMethod2), arrayListOf(column1, column2), arrayListOf(column1, column2)
        )

        val testObserver = appSettingsSynchronizer.getCurrentAppSettings().test()
        testObserver.awaitTerminalEvent()
        testObserver.assertNoErrors()
            .assertComplete()
            .assertResult(appSettings)
    }

    @Test
    fun checkCategoriesWhenSameTest() {
        // Note: while checking categories, we need to check just uuid+name+code (ignore id because it's local)
        val testObserver =
            appSettingsSynchronizer.checkCategoriesMatch(arrayListOf(category2, CategoryBuilderFactory(category1).setId(152).build()))
                .test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertResult(true)
    }

    @Test
    fun checkCategoriesWhenNotSameTest() {
        val testObserver = appSettingsSynchronizer.checkCategoriesMatch(
            arrayListOf(
                category2,
                CategoryBuilderFactory(category1).setUuid(UUID.randomUUID()).build()
            )
        ).test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertResult(false)
    }

    @Test
    fun checkPaymentMethodsWhenSameTest() {
        // Note: while checking payment methods, we need to check just uuid+method (ignore id because it's local)
        val testObserver = appSettingsSynchronizer.checkPaymentMethodsMatch(
            arrayListOf(
                paymentMethod2,
                PaymentMethodBuilderFactory(paymentMethod1).setId(152).build()
            )
        )
            .test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertResult(true)
    }

    @Test
    fun checkPaymentMethodsWhenNotSameTest() {
        val testObserver = appSettingsSynchronizer.checkPaymentMethodsMatch(
            arrayListOf(paymentMethod2, PaymentMethodBuilderFactory(paymentMethod1).setUuid(UUID.randomUUID()).build())
        )
            .test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertResult(false)
    }

    @Test
    fun checkColumnsWhenSameTest() {
        // Note: while checking columns, we need to check just uuid+type
        val testObserver = appSettingsSynchronizer.checkCsvColumnsMatch(arrayListOf(column2, column1)).test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertResult(true)
    }

    @Test
    fun checkColumnsWhenSameButDifferentSizeTest() {
        val testObserver = appSettingsSynchronizer.checkCsvColumnsMatch(arrayListOf(column2)).test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertResult(true)
    }

    @Test
    fun checkColumnsWhenNotSameTest() {
        val testObserver = appSettingsSynchronizer.checkPdfColumnsMatch(arrayListOf(column2, column3)).test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertResult(false)
    }

    @Test
    fun applyCategoriesWhenSame() {
        val testObserver = appSettingsSynchronizer.applyCategories(arrayListOf(category2, category1)).test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()

        verify(categoriesTableController, never()).update(any(), any(), any())
        verify(categoriesTableController, never()).insert(any(), any())
    }

    @Test
    fun applyCategoriesWhenChanged() {
        val category2Changed = CategoryBuilderFactory(category2).setName("another name").build()
        val testObserver = appSettingsSynchronizer.applyCategories(arrayListOf(category1, category2Changed)).test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()

        verify(categoriesTableController, times(1)).update(eq(category2), eq(category2Changed), any())
        verify(categoriesTableController, never()).insert(any(), any())
    }

    @Test
    fun applyCategoriesWhenNotFound() {
        val category3 = CategoryBuilderFactory().build()
        val testObserver = appSettingsSynchronizer.applyCategories(arrayListOf(category1, category3)).test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()

        verify(categoriesTableController, never()).update(any(), any(), any())
        verify(categoriesTableController, times(1)).insert(eq(category3), any())
    }
}
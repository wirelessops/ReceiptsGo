package co.smartreceipts.android.columns.ordering

import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.Column
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.factory.ColumnBuilderFactory
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions
import co.smartreceipts.android.persistence.database.controllers.impl.ColumnTableController
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.workers.reports.ReportResourcesManager
import com.hadisatrio.optional.Optional
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class AbstractColumnsOrdererTest {

    private lateinit var abstractColumnsOrderer: AbstractColumnsOrderer

    @Mock
    private lateinit var columnTableController: ColumnTableController

    private lateinit var definitions: ReceiptColumnDefinitions

    private val scheduler = Schedulers.trampoline()

    @Mock
    private lateinit var reportResourcesManager: ReportResourcesManager

    @Mock
    private lateinit var preferences: UserPreferenceManager

    @Mock
    private lateinit var dateFormatter: DateFormatter

    private lateinit var column0 : Column<Receipt>

    private lateinit var column1 : Column<Receipt>

    private lateinit var column2 : Column<Receipt>

    @Mock
    private lateinit var columnUpdateResult : Column<Receipt>

    @Mock
    private lateinit var columnInsertResult : Column<Receipt>

    private inner class TestAbstractColumnsOrderer(columnTableController: ColumnTableController,
                                                   receiptColumnDefinitions: ReceiptColumnDefinitions,
                                                   scheduler: Scheduler) : AbstractColumnsOrderer(columnTableController, receiptColumnDefinitions, scheduler)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        definitions = ReceiptColumnDefinitions(reportResourcesManager, preferences, dateFormatter)
        column0 = ColumnBuilderFactory(definitions).setCustomOrderId(0).setColumnType(ReceiptColumnDefinitions.ActualDefinition.NAME.columnType).build()
        column1 = ColumnBuilderFactory(definitions).setCustomOrderId(1).setColumnType(ReceiptColumnDefinitions.ActualDefinition.PRICE.columnType).build()
        column2 = ColumnBuilderFactory(definitions).setCustomOrderId(2).setColumnType(ReceiptColumnDefinitions.ActualDefinition.TAX.columnType).build()
        whenever(columnTableController.get()).thenReturn(Single.just(listOf(column0, column1, column2)))
        whenever(columnTableController.insert(any(), any())).thenReturn(Observable.just(Optional.of(columnInsertResult)))
        whenever(columnTableController.update(any(), any(), any())).thenReturn(Observable.just(Optional.of(columnUpdateResult)))
        abstractColumnsOrderer = TestAbstractColumnsOrderer(columnTableController, definitions, scheduler)
    }

    @Test
    fun insertColumnAfterColumn0() {
        val definitionToInsert = ReceiptColumnDefinitions.ActualDefinition.ID
        val afterDefinition = ReceiptColumnDefinitions.ActualDefinition.NAME
        abstractColumnsOrderer.insertColumnAfter(definitionToInsert, afterDefinition)
                .test()
                .assertNoErrors()
                .assertComplete()

        val insertedColumn = ColumnBuilderFactory(definitions).setCustomOrderId(1).setColumnType(definitionToInsert.columnType).build()
        val column0Update = ColumnBuilderFactory(definitions, column0).setCustomOrderId(0).build()
        val column1Update = ColumnBuilderFactory(definitions, column1).setCustomOrderId(2).build()
        val column2Update = ColumnBuilderFactory(definitions, column2).setCustomOrderId(3).build()
        verify(columnTableController).update(column0, column0Update, DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(columnTableController).update(column1, column1Update, DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(columnTableController).update(column2, column2Update, DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(columnTableController).insert(insertedColumn, DatabaseOperationMetadata())
    }

    @Test
    fun insertColumnAfterColumn1() {
        val definitionToInsert = ReceiptColumnDefinitions.ActualDefinition.ID
        val afterDefinition = ReceiptColumnDefinitions.ActualDefinition.PRICE
        abstractColumnsOrderer.insertColumnAfter(definitionToInsert, afterDefinition)
                .test()
                .assertNoErrors()
                .assertComplete()

        val insertedColumn = ColumnBuilderFactory(definitions).setCustomOrderId(2).setColumnType(definitionToInsert.columnType).build()
        val column0Update = ColumnBuilderFactory(definitions, column0).setCustomOrderId(0).build()
        val column1Update = ColumnBuilderFactory(definitions, column1).setCustomOrderId(1).build()
        val column2Update = ColumnBuilderFactory(definitions, column2).setCustomOrderId(3).build()
        verify(columnTableController).update(column0, column0Update, DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(columnTableController).update(column1, column1Update, DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(columnTableController).update(column2, column2Update, DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(columnTableController).insert(insertedColumn, DatabaseOperationMetadata())
    }

    @Test
    fun insertColumnAfterColumn2() {
        val definitionToInsert = ReceiptColumnDefinitions.ActualDefinition.ID
        val afterDefinition = ReceiptColumnDefinitions.ActualDefinition.TAX
        abstractColumnsOrderer.insertColumnAfter(definitionToInsert, afterDefinition)
                .test()
                .assertNoErrors()
                .assertComplete()

        val insertedColumn = ColumnBuilderFactory(definitions).setCustomOrderId(3).setColumnType(definitionToInsert.columnType).build()
        val column0Update = ColumnBuilderFactory(definitions, column0).setCustomOrderId(0).build()
        val column1Update = ColumnBuilderFactory(definitions, column1).setCustomOrderId(1).build()
        val column2Update = ColumnBuilderFactory(definitions, column2).setCustomOrderId(2).build()
        verify(columnTableController).update(column0, column0Update, DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(columnTableController).update(column1, column1Update, DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(columnTableController).update(column2, column2Update, DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(columnTableController).insert(insertedColumn, DatabaseOperationMetadata())
    }

    @Test
    fun insertColumnAfterColumnThatIsNotPresent() {
        val definitionToInsert = ReceiptColumnDefinitions.ActualDefinition.ID
        val afterDefinition = ReceiptColumnDefinitions.ActualDefinition.REIMBURSABLE
        abstractColumnsOrderer.insertColumnAfter(definitionToInsert, afterDefinition)
                .test()
                .assertNoErrors()
                .assertComplete()

        val insertedColumn = ColumnBuilderFactory(definitions).setCustomOrderId(3).setColumnType(definitionToInsert.columnType).build()
        val column0Update = ColumnBuilderFactory(definitions, column0).setCustomOrderId(0).build()
        val column1Update = ColumnBuilderFactory(definitions, column1).setCustomOrderId(1).build()
        val column2Update = ColumnBuilderFactory(definitions, column2).setCustomOrderId(2).build()
        verify(columnTableController).update(column0, column0Update, DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(columnTableController).update(column1, column1Update, DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(columnTableController).update(column2, column2Update, DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(columnTableController).insert(insertedColumn, DatabaseOperationMetadata())
    }

}
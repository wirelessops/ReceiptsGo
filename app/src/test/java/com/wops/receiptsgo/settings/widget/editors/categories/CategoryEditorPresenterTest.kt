package com.wops.receiptsgo.settings.widget.editors.categories

import android.os.Bundle
import com.wops.receiptsgo.model.Category
import com.wops.receiptsgo.model.factory.CategoryBuilderFactory
import com.wops.receiptsgo.persistence.database.controllers.impl.CategoriesTableController
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata
import com.nhaarman.mockitokotlin2.*
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CategoryEditorPresenterTest {

    @Mock
    private lateinit var view: CategoryEditorView

    @Mock
    private lateinit var router: CategoryEditorRouter

    @Mock
    private lateinit var categoriesTableController: CategoriesTableController

    private val saveClickStream = PublishSubject.create<Any>()

    private val cancelClickStream = PublishSubject.create<Any>()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(view.getCancelClickStream()).thenReturn(cancelClickStream)
        whenever(view.getSaveClickStream()).thenReturn(saveClickStream)
    }

    @Test
    fun subscribeAndCancel() {
        val presenter = CategoryEditorPresenter(view, router, categoriesTableController, null, null)
        presenter.subscribe()
        cancelClickStream.onNext(Any())
        verify(router).dismissEditor()
    }

    @Test
    fun subscribeForNewCategoryWithoutState() {
        val presenter = CategoryEditorPresenter(view, router, categoriesTableController, null, null)
        presenter.subscribe()
        verify(view).displayCategory(null)
    }

    @Test
    fun subscribeForNewCategoryWithState() {
        val presenter = CategoryEditorPresenter(view, router, categoriesTableController, null, Bundle())
        presenter.subscribe()
        verify(view, never()).displayCategory(any())
    }

    @Test
    fun subscribeForExistingCategoryWithoutState() {
        val category = CategoryBuilderFactory().build()
        val presenter = CategoryEditorPresenter(view, router, categoriesTableController, category, null)
        presenter.subscribe()
        verify(view).displayCategory(category)
    }

    @Test
    fun subscribeForExistingCategoryWithState() {
        val category = CategoryBuilderFactory().build()
        val presenter = CategoryEditorPresenter(view, router, categoriesTableController, category, Bundle())
        presenter.subscribe()
        verify(view, never()).displayCategory(any())
    }

    @Test
    fun subscribeAndSaveInsertsNewCategoryWithoutExistingCategoryAndWithoutSavedState() {
        val newName = "newName"
        val newCode = "newCode"
        whenever(view.getName()).thenReturn(newName)
        whenever(view.getCode()).thenReturn(newCode)
        val newCategory = CategoryBuilderFactory().setName(newName).setCode(newCode).setCustomOrderId(Long.MAX_VALUE).build()
        val presenter = CategoryEditorPresenter(view, router, categoriesTableController, null, null)
        presenter.subscribe()
        saveClickStream.onNext(Any())
        verify(categoriesTableController).insert(newCategory, DatabaseOperationMetadata())
        verify(router).dismissEditor()
    }

    @Test
    fun subscribeAndSaveInsertsNewCategoryWithoutExistingCategoryAndWithSavedState() {
        val newName = "newName"
        val newCode = "newCode"
        whenever(view.getName()).thenReturn(newName)
        whenever(view.getCode()).thenReturn(newCode)
        val newCategory = CategoryBuilderFactory().setName(newName).setCode(newCode).setCustomOrderId(Long.MAX_VALUE).build()
        val presenter = CategoryEditorPresenter(view, router, categoriesTableController, null, Bundle())
        presenter.subscribe()
        saveClickStream.onNext(Any())
        verify(categoriesTableController).insert(newCategory, DatabaseOperationMetadata())
        verify(router).dismissEditor()
    }

    @Test
    fun subscribeAndUnsubscribeIgnoresSaveEventsForNewCategory() {
        val newName = "newName"
        val newCode = "newCode"
        whenever(view.getName()).thenReturn(newName)
        whenever(view.getCode()).thenReturn(newCode)
        val presenter = CategoryEditorPresenter(view, router, categoriesTableController, null, null)
        presenter.subscribe()
        presenter.unsubscribe()
        saveClickStream.onNext(Any())
        verifyZeroInteractions(categoriesTableController)
        verify(router, never()).dismissEditor()
    }

    @Test
    fun subscribeAndSaveUpdatesExistingCategoryWithoutSavedState() {
        val existingCategory = CategoryBuilderFactory().setName("name").setCode("code").build()
        val newName = "newName"
        val newCode = "newCode"
        whenever(view.getName()).thenReturn(newName)
        whenever(view.getCode()).thenReturn(newCode)
        val newCategory = CategoryBuilderFactory(existingCategory).setName(newName).setCode(newCode).build()
        val presenter = CategoryEditorPresenter(view, router, categoriesTableController, existingCategory, null)
        presenter.subscribe()
        saveClickStream.onNext(Any())
        verify(categoriesTableController).update(existingCategory, newCategory, DatabaseOperationMetadata())
        verify(router).dismissEditor()
    }

    @Test
    fun subscribeAndSaveUpdatesExistingCategoryWithSavedState() {
        val existingCategory = CategoryBuilderFactory().setName("name").setCode("code").build()
        val newName = "newName"
        val newCode = "newCode"
        whenever(view.getName()).thenReturn(newName)
        whenever(view.getCode()).thenReturn(newCode)
        val newCategory = CategoryBuilderFactory(existingCategory).setName(newName).setCode(newCode).build()
        val presenter = CategoryEditorPresenter(view, router, categoriesTableController, existingCategory, Bundle())
        presenter.subscribe()
        saveClickStream.onNext(Any())
        verify(categoriesTableController).update(existingCategory, newCategory, DatabaseOperationMetadata())
        verify(router).dismissEditor()
    }

    @Test
    fun subscribeAndUnsubscribeIgnoresSaveEventsForExistingCategory() {
        val existingCategory = CategoryBuilderFactory().setName("name").setCode("code").build()
        val newName = "newName"
        val newCode = "newCode"
        whenever(view.getName()).thenReturn(newName)
        whenever(view.getCode()).thenReturn(newCode)
        val presenter = CategoryEditorPresenter(view, router, categoriesTableController, existingCategory, null)
        presenter.subscribe()
        presenter.unsubscribe()
        saveClickStream.onNext(Any())
        verifyZeroInteractions(categoriesTableController)
        verify(router, never()).dismissEditor()
    }

    @Test
    fun subscribeAndCancelDismissesTheEditor() {
        val presenter = CategoryEditorPresenter(view, router, categoriesTableController, null, null)
        presenter.subscribe()
        cancelClickStream.onNext(Any())
        verify(router).dismissEditor()
    }

    @Test
    fun subscribeAndUnsubscribeThenCancelDoesNotDismissTheEditor() {
        val presenter = CategoryEditorPresenter(view, router, categoriesTableController, null, null)
        presenter.subscribe()
        presenter.unsubscribe()
        cancelClickStream.onNext(Any())
        verify(router, never()).dismissEditor()
    }
}
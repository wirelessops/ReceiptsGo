package com.wops.receiptsgo.settings.widget.editors.categories

import android.os.Bundle
import com.wops.analytics.log.Logger
import com.wops.receiptsgo.model.Category
import com.wops.receiptsgo.model.factory.CategoryBuilderFactory
import com.wops.receiptsgo.persistence.database.controllers.impl.CategoriesTableController
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata
import com.wops.receiptsgo.widget.mvp.BasePresenter
import io.reactivex.Observable

/**
 * Defines a presenter contract for the [CategoryEditorView]
 */
class CategoryEditorPresenter(view: CategoryEditorView,
                              private val router: CategoryEditorRouter,
                              private val categoriesTableController: CategoriesTableController,
                              private val existingCategory: Category?,
                              private val savedInstanceState: Bundle?) : BasePresenter<CategoryEditorView>(view) {

    override fun subscribe() {
        if (existingCategory == null) {
            // The stream for creating new categories
            compositeDisposable.add(view.getSaveClickStream()
                    .flatMap {
                        Observable.just(CategoryBuilderFactory()
                                .setName(view.getName())
                                .setCode(view.getCode())
                                .setCustomOrderId(Long.MAX_VALUE)
                                .build())
                    }
                    .doOnNext { Logger.info(this, "Creating a new category") }
                    .subscribe {
                        categoriesTableController.insert(it, DatabaseOperationMetadata())
                        router.dismissEditor()
                    })
        } else {
            // The stream for updating existing categories
            compositeDisposable.add(view.getSaveClickStream()
                    .flatMap {
                        Observable.just(CategoryBuilderFactory(existingCategory)
                                .setName(view.getName())
                                .setCode(view.getCode())
                                .build())
                    }
                    .doOnNext { Logger.info(this, "Updating an existing category") }
                    .subscribe {
                        categoriesTableController.update(existingCategory, it, DatabaseOperationMetadata())
                        router.dismissEditor()
                    })
        }

        if (savedInstanceState == null) {
            // Only indicate the display action if there's no saved state (which might include user input)
            view.displayCategory(existingCategory)
        }

        compositeDisposable.add(view.getCancelClickStream()
                .doOnNext { Logger.info(this, "Cancelling the category editor") }
                .subscribe {
                    router.dismissEditor()
                })
    }

}
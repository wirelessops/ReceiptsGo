package com.wops.receiptsgo.persistence.database.controllers.impl;

import javax.inject.Inject;

import com.wops.analytics.Analytics;
import com.wops.core.di.scopes.ApplicationScope;
import com.wops.receiptsgo.model.Category;
import com.wops.receiptsgo.persistence.DatabaseHelper;
import com.wops.receiptsgo.persistence.database.controllers.alterations.CategoriesTableActionAlterations;

@ApplicationScope
public class CategoriesTableController extends AbstractTableController<Category> {

    @Inject
    public CategoriesTableController(DatabaseHelper databaseHelper, Analytics analytics) {
        super(databaseHelper.getCategoriesTable(), new CategoriesTableActionAlterations(databaseHelper.getReceiptsTable()), analytics);
    }
}

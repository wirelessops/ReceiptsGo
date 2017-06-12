package co.smartreceipts.android.model.impl.columns.categories;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.comparators.ColumnNameComparator;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import wb.android.flex.Flex;

public class CategoryColumnDefinitions implements ColumnDefinitions<SumCategoryGroupingResult> {

    private enum ActualDefinition {
        NAME(R.string.category_name_field),
        CODE(R.string.category_code_field),
        PRICE(R.string.category_price_field),
        TAX(R.string.category_tax_field),
        CURRENCY(R.string.category_currency_field);

        private final int stringResId;

        ActualDefinition(int stringResId) {
            this.stringResId = stringResId;
        }

        @StringRes
        public final int getStringResId() {
            return stringResId;
        }

    }

    private final ActualDefinition[] actualDefinitions;
    private final Flex flex;
    private final Context context;


    public CategoryColumnDefinitions(Flex flex, Context context) {
        this.flex = flex;
        this.context = context;
        this.actualDefinitions = ActualDefinition.values();
    }

    @Override
    public Column<SumCategoryGroupingResult> getColumn(int id, @NonNull String definitionName, @NonNull SyncState syncState) {
        for (int i = 0; i < actualDefinitions.length; i++) {
            final ActualDefinition definition = actualDefinitions[i];
            if (definitionName.equals(getColumnNameFromStringResId(definition.getStringResId()))) {
                return getColumnFromClass(id, definition, definitionName, syncState);
            }
        }
        return null;
    }

    @NonNull
    @Override
    public List<Column<SumCategoryGroupingResult>> getAllColumns() {
        final ArrayList<AbstractColumnImpl<SumCategoryGroupingResult>> columns = new ArrayList<>(actualDefinitions.length);
        for (int i = 0; i < actualDefinitions.length; i++) {
            final ActualDefinition definition = actualDefinitions[i];
            final AbstractColumnImpl<SumCategoryGroupingResult> column = getColumnFromClass(Column.UNKNOWN_ID, definition, getColumnNameFromStringResId(definition.getStringResId()), new DefaultSyncState());
            if (column != null) {
                columns.add(column);
            }
        }
        Collections.sort(columns, new ColumnNameComparator<AbstractColumnImpl<SumCategoryGroupingResult>>());
        return new ArrayList<Column<SumCategoryGroupingResult>>(columns);
    }

    @NonNull
    @Override
    public Column<SumCategoryGroupingResult> getDefaultInsertColumn() {
        return new BlankColumn<>(Column.UNKNOWN_ID, context.getString(R.string.column_item_blank), new DefaultSyncState());
    }


    private AbstractColumnImpl<SumCategoryGroupingResult> getColumnFromClass(int id, @NonNull ActualDefinition definition, @NonNull String definitionName, @NonNull SyncState syncState) {
        switch (definition) {
            case NAME:
                return new CategoryNameColumn(id, definitionName, syncState);
            case CODE:
                return new CategoryCodeColumn(id, definitionName, syncState);
            case PRICE:
                return new CategoryPriceColumn(id, definitionName, syncState);
            case TAX:
                return new CategoryTaxColumn(id, definitionName, syncState);
            case CURRENCY:
                return new CategoryCurrencyColumn(id, definitionName, syncState);
            default:
                throw new IllegalArgumentException("Unknown definition type: " + definition);
        }
    }

    private String getColumnNameFromStringResId(int stringResId) {
        if (flex != null) {
            return flex.getString(context, stringResId);
        } else {
            return context.getString(stringResId);
        }
    }
}

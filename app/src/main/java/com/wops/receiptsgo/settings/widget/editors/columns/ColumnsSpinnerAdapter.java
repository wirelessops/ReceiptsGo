package com.wops.receiptsgo.settings.widget.editors.columns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import java.util.List;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.workers.reports.ReportResourcesManager;

public class ColumnsSpinnerAdapter<T> extends ArrayAdapter<Column<T>> {

    private static final int SPINNER_ITEM_RES = R.layout.simple_spinner_item;
    private static final int SPINNER_DROPDOWN_ITEM_RES = R.layout.simple_spinner_dropdown_item;

    private final ReportResourcesManager reportResourcesManager;

    public ColumnsSpinnerAdapter(@NonNull ReportResourcesManager reportResourcesManager, @NonNull List<Column<T>> objects) {
        super(reportResourcesManager.getLocalizedContext(), SPINNER_ITEM_RES, objects);

        this.reportResourcesManager = Preconditions.checkNotNull(reportResourcesManager);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getSimpleView(position, convertView, parent, false);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getSimpleView(position, convertView, parent, true);
    }

    private View getSimpleView(int position, @Nullable View convertView, @NonNull ViewGroup parent, boolean isDropDown) {
        final TextView textView;

        if (convertView == null) {
            textView = (TextView) LayoutInflater.from(getContext()).inflate(isDropDown ? SPINNER_DROPDOWN_ITEM_RES : SPINNER_ITEM_RES,
                    parent, false);
        } else {
            textView = (TextView) convertView;
        }

        textView.setText(reportResourcesManager.getFlexString(getItem(position).getHeaderStringResId()));

        return textView;
    }
}

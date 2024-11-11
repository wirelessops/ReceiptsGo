package com.wops.receiptsgo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import java.util.List;

import com.wops.receiptsgo.R;

public class FooterButtonArrayAdapter<T> extends ArrayAdapter<T> {

    @StringRes
    private final int buttonTextResId;

    private final View.OnClickListener listener;

    public FooterButtonArrayAdapter(@NonNull Context context, @NonNull List<T> objects,
                                    @StringRes int buttonTextResId, @Nullable View.OnClickListener onFooterButtonClickListener) {
        super(context, android.R.layout.simple_spinner_item, objects);
        super.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.buttonTextResId = buttonTextResId;
        this.listener = onFooterButtonClickListener;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Nullable
    @Override
    public T getItem(int position) {
        return position < super.getCount() ? super.getItem(position) : null;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (isFooter(position)) {
            // Note: despite having #getItem(int) tagged as nullable, null values causes a crash... So we use this instead
            final View view;
            if (convertView == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);
            } else {
                // Note: We don't update the text or anything here, since this item will never actually get selected
                view = convertView;
            }
            return view;
        } else {
            // Create a standard view
            return super.getView(position, convertView, parent);
        }
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (isFooter(position)) {
            final Button button = new Button(getContext());
            button.setText(buttonTextResId);
            button.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background));
            TextViewCompat.setTextAppearance(button, R.style.Widget_SmartReceipts_TextView_Button_Tertiary);

            button.setOnClickListener(listener);

            return button;
        } else {
            // Create view for standard spinner item
            return super.getDropDownView(position, null, parent);
        }
    }

    public void update(List<T> items) {
        super.clear();
        super.addAll(items);
    }

    /**
     * Checks if the position is at the footer position
     *
     * @param position the current position
     * @return {@code true} if this is the footer. {@code false} otherwise
     */
    private boolean isFooter(int position) {
        return position == getCount() - 1;
    }
}

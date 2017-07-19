package co.smartreceipts.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import java.util.List;

import co.smartreceipts.android.R;


public class FooterButtonArrayAdapter<T> extends ArrayAdapter<T> {

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


    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (position == getCount() - 1) {
            //create view for footer button
            Button button = new Button(getContext());
            button.setText(buttonTextResId);
            button.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.card_background));
            button.setTextAppearance(getContext(), R.style.Widget_SmartReceipts_TextView_Button_Tertiary);

            button.setOnClickListener(listener);

            return button;
        } else {
            //create view for standard spinner item
            return super.getDropDownView(position, null, parent);
        }
    }
}

package wb.android.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;

/*
 * By default, AlertDialog windows are closed whenever a button is clicked.
 * This class is essentially just a wrap-around hack, so that the dialog
 * will only be closed when you explicitly close it.
 */
public class BetterDialogBuilder extends AlertDialog.Builder {

    private LongLivedOnClickListener _positiveListener, _neutralListener, _negativeListener;

    private DialogInterface.OnClickListener _defaultListener = new DialogInterface.OnClickListener() {
        @Override public void onClick(DialogInterface dialog, int which) {}
    };

    public BetterDialogBuilder(Context context) {
        super(context);
    }

    public final BetterDialogBuilder setLongLivedPositiveButton(CharSequence text, LongLivedOnClickListener listener) {
        _positiveListener = listener;
        super.setPositiveButton(text, _defaultListener);
        return this;
    }

    public final BetterDialogBuilder setLongLivedNeutralButton(CharSequence text, LongLivedOnClickListener listener) {
        _neutralListener = listener;
        super.setNeutralButton(text, _defaultListener);
        return this;
    }

    public final BetterDialogBuilder setLongLivedNegativeButton(CharSequence text, LongLivedOnClickListener listener) {
        _negativeListener = listener;
        super.setNegativeButton(text, _defaultListener);
        return this;
    }

    @Override
    public BetterDialogBuilder setTitle(int titleId) {
        super.setTitle(titleId);
        return this;
    }

    @Override
    public BetterDialogBuilder setTitle(CharSequence title) {
        super.setTitle(title);
        return this;
    }

    @Override
    public BetterDialogBuilder setCustomTitle(View customTitleView) {
        super.setCustomTitle(customTitleView);
        return this;
    }

    @Override
    public BetterDialogBuilder setMessage(int messageId) {
        super.setMessage(messageId);
        return this;
    }

    @Override
    public BetterDialogBuilder setMessage(CharSequence message) {
        super.setMessage(message);
        return this;
    }

    @Override
    public BetterDialogBuilder setIcon(int iconId) {
        super.setIcon(iconId);
        return this;
    }

    @Override
    public BetterDialogBuilder setIcon(Drawable icon) {
        super.setIcon(icon);
        return this;
    }

    @Override
    public BetterDialogBuilder setPositiveButton(int textId, final OnClickListener listener) {
        super.setPositiveButton(textId, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setPositiveButton(CharSequence text, final OnClickListener listener) {
        super.setPositiveButton(text, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setNegativeButton(int textId, final OnClickListener listener) {
        super.setNegativeButton(textId, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setNegativeButton(CharSequence text, final OnClickListener listener) {
        super.setNegativeButton(text, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setNeutralButton(int textId, final OnClickListener listener) {
        super.setNeutralButton(textId, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setNeutralButton(CharSequence text, final OnClickListener listener) {
        super.setNeutralButton(text, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        return this;
    }

    @Override
    public BetterDialogBuilder setOnCancelListener(OnCancelListener onCancelListener) {
        super.setOnCancelListener(onCancelListener);
        return this;
    }

    @Override
    public BetterDialogBuilder setOnKeyListener(OnKeyListener onKeyListener) {
        super.setOnKeyListener(onKeyListener);
        return this;
    }

    @Override
    public BetterDialogBuilder setItems(int itemsId, final OnClickListener listener) {
        super.setItems(itemsId, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setItems(CharSequence[] items, final OnClickListener listener) {
        super.setItems(items, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setAdapter(final ListAdapter adapter, final OnClickListener listener) {
        super.setAdapter(adapter, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setCursor(final Cursor cursor, final OnClickListener listener, String labelColumn) {
        super.setCursor(cursor, listener, labelColumn);
        return this;
    }

    @Override
    public BetterDialogBuilder setMultiChoiceItems(int itemsId, boolean[] checkedItems, final OnMultiChoiceClickListener listener) {
        super.setMultiChoiceItems(itemsId, checkedItems, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, final OnMultiChoiceClickListener listener) {
        super.setMultiChoiceItems(items, checkedItems, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setMultiChoiceItems(Cursor cursor, String isCheckedColumn, String labelColumn, final OnMultiChoiceClickListener listener) {
        super.setMultiChoiceItems(cursor, isCheckedColumn, labelColumn, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setSingleChoiceItems(int itemsId, int checkedItem, final OnClickListener listener) {
        super.setSingleChoiceItems(itemsId, checkedItem, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setSingleChoiceItems(Cursor cursor, int checkedItem, String labelColumn, final OnClickListener listener) {
        setSingleChoiceItems(cursor, checkedItem, labelColumn, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setSingleChoiceItems(CharSequence[] items, int checkedItem, final OnClickListener listener) {
        super.setSingleChoiceItems(items, checkedItem, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setSingleChoiceItems(ListAdapter adapter, int checkedItem, final OnClickListener listener) {
        super.setSingleChoiceItems(adapter, checkedItem, listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setOnItemSelectedListener(final AdapterView.OnItemSelectedListener listener) {
        super.setOnItemSelectedListener(listener);
        return this;
    }

    @Override
    public BetterDialogBuilder setView(View view) {
        super.setView(view);
        return this;
    }

    @Override
    public BetterDialogBuilder setInverseBackgroundForced(boolean useInverseBackground) {
        super.setInverseBackgroundForced(useInverseBackground);
        return this;
    }

    @Override
    public AlertDialog create() {
        return super.create();
    }

    @Override
    public AlertDialog show() {
        final AlertDialog dialog = create();
        dialog.show();
        if (_positiveListener != null) {
            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            _positiveListener.set(dialog, DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(_positiveListener);
        }
        if (_neutralListener != null) {
            Button neutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            _neutralListener.set(dialog, DialogInterface.BUTTON_NEUTRAL);
            neutralButton.setOnClickListener(_neutralListener);
        }
        if (_negativeListener != null) {
            Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            _negativeListener.set(dialog, DialogInterface.BUTTON_NEGATIVE);
            negativeButton.setOnClickListener(_negativeListener);
        }
        return dialog;
    }
}

package wb.android.dialog.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class EditTextDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

	public static final String TAG = EditTextDialogFragment.class.getSimpleName();
	
	private static final String BUNDLE_CURRENT_TEXT = "bundle_current_text";
	private static final String KEY_TITLE = "key_Title";
	private static final String KEY_TEXT = "key_text";
	private static final String KEY_HINT = "key_hint";
	private static final String KEY_POSITIVE_BUTTON = "key_positive_button";
	private static final String KEY_NEGATIVE_BUTTON = "key_negative_button";
	private static final String KEY_CHECKED = "key_checked";
	private static final String KEY_CHECKBOX_TEXT = "key_checkbox_text";
	private EditText mEditText;
	private CheckBox mCheckBox;
	private boolean mCheckBoxChecked;
	private String mTitle, mText, mHint, mPositiveButtonText, mNegativeButtonText, mCheckBoxText;
	private OnClickListener mOnClickListener, mOnClickListenerArgument;
	
	/**
	 * A call back listener to track when this dialog was click
	 */
	public interface OnClickListener {
		
		/**
		 * Called whenever one of the dialog buttons is clicked
		 * 
		 * @param text - the user input text
		 * @param which - which {@link DialogInterface} button this is.
		 */
		public void onClick(final String text, final boolean isReimbursable, final int which);
	}
	
	public static EditTextDialogFragment newInstance(final String title, final String text, final String hint, final String positiveButtonText,
													 final String negativeButtonText, final boolean reimbursable, final String checkboxText, final OnClickListener listener) {
		final Bundle args = new Bundle();
		args.putString(KEY_TITLE, title);
		args.putString(KEY_TEXT, text);
		args.putString(KEY_HINT, hint);
		args.putString(KEY_POSITIVE_BUTTON, positiveButtonText);
		args.putString(KEY_NEGATIVE_BUTTON, negativeButtonText);
		args.putBoolean(KEY_CHECKED, reimbursable);
		args.putString(KEY_CHECKBOX_TEXT, checkboxText);
		final EditTextDialogFragment fragment = new EditTextDialogFragment();
		fragment.setArguments(args);
		fragment.setOnClickListenerArgument(listener);
		return fragment;
	}
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		mOnClickListener = getOnClickListenerArgument();
		mTitle = getArgumentString(KEY_TITLE, "");
		mHint = getArgumentString(KEY_HINT, "");
		mPositiveButtonText = getArgumentString(KEY_POSITIVE_BUTTON, "");
		mNegativeButtonText = getArgumentString(KEY_NEGATIVE_BUTTON, "");
		mCheckBoxText = getArgumentString(KEY_CHECKBOX_TEXT, "");
		mCheckBoxChecked = getArguments().getBoolean(KEY_CHECKED, false);
	}
	
	@Override
	@NonNull
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mText = savedInstanceState.getString(BUNDLE_CURRENT_TEXT);
		} else {
			mText = getArgumentString(KEY_TEXT, "");
		}

		LinearLayout mLinearLayout = new LinearLayout(getActivity());
		mLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		mLinearLayout.setOrientation(LinearLayout.VERTICAL);
		
		mEditText = new EditText(getActivity());
		mEditText.setHint(mHint);
		mEditText.setText(mText);

		mCheckBox = new CheckBox(getActivity());
		mCheckBox.setText(mCheckBoxText);
		mCheckBox.setChecked(mCheckBoxChecked);

		mLinearLayout.addView(mEditText);
		mLinearLayout.addView(mCheckBox);
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(mTitle);
		builder.setView(mLinearLayout);
		builder.setPositiveButton(mPositiveButtonText, this);
		builder.setNegativeButton(mNegativeButtonText, this);
		return builder.create();
	}
	
	@Override
	public void onSaveInstanceState(final Bundle bundle) {
		bundle.putString(BUNDLE_CURRENT_TEXT, mEditText.getText().toString());
		super.onSaveInstanceState(bundle);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
		if (mOnClickListener != null) {
			mOnClickListener.onClick(mEditText.getText().toString(), mCheckBox.isChecked(), which);
		}
	}
	
	/**
	 * Follows the same approach as normally getting fragment arguments, but provides a default value
	 * 
	 * @param key - the key to check
	 * @param defaultValue - the default value if this key cannot be found
	 * @return the value or the default value if not found
	 */
	private String getArgumentString(final String key, final String defaultValue) {
		if (getArguments().containsKey(key)) {
			return getArguments().getString(key);
		} else {
			return defaultValue;
		}
	}
	
	private void setOnClickListenerArgument(final OnClickListener onClickListener) {
		mOnClickListenerArgument = onClickListener;
	}
	
	private OnClickListener getOnClickListenerArgument() {
		return mOnClickListenerArgument;
	}

	
}

package wb.android.preferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

public class SummaryEditTextPreference extends EditTextPreference implements Preference.OnPreferenceChangeListener {

	private OnPreferenceChangeListener mOnPreferenceChangeListener;

    /**
     * All this enabled stuff is a nifty hack for Pro subscription upsells
     */
    private boolean mAppearsEnabled = true;

	public SummaryEditTextPreference(Context context) {
		super(context);
		super.setOnPreferenceChangeListener(this); //Must use the super's method here (since we overwrite)
	}
	
	public SummaryEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		super.setOnPreferenceChangeListener(this); //Must use the super's method here (since we overwrite)
	}
	
	public SummaryEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		super.setOnPreferenceChangeListener(this); //Must use the super's method here (since we overwrite)
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		preference.setSummary(getSummary());
		if (mOnPreferenceChangeListener != null) {
			return mOnPreferenceChangeListener.onPreferenceChange(preference, newValue);
		}
		return true;
	}
	
	@Override
	public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
		mOnPreferenceChangeListener = onPreferenceChangeListener;
	}
	
	@Override
	public CharSequence getSummary() {
		return getText();
	}

    public void setAppearsEnabled(boolean appearsEnabled) {
        mAppearsEnabled = appearsEnabled;
    }

    @Override
    protected void onClick() {
        if (mAppearsEnabled) {
            super.onClick();
        }
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        boolean viewEnabled = isEnabled() && mAppearsEnabled;
        enableView(view, viewEnabled);
    }

    private void enableView(@NonNull View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                enableView(viewGroup.getChildAt(i), enabled);
            }
        }
    }

}
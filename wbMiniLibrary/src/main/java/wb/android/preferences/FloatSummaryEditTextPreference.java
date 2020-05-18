package wb.android.preferences;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class FloatSummaryEditTextPreference extends FloatEditTextPreference implements DeactivatablePreference, Preference.OnPreferenceChangeListener {

    private OnPreferenceChangeListener mOnPreferenceChangeListener;

    private ViewStateChanger stateChanger = new ViewStateChanger();

    public FloatSummaryEditTextPreference(Context context) {
        super(context);
        super.setOnPreferenceChangeListener(this); //Must use the super's method here (since we overwrite)
    }

    public FloatSummaryEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnPreferenceChangeListener(this); //Must use the super's method here (since we overwrite)
    }

    public FloatSummaryEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
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

    @Override
    public void setAppearsEnabled(boolean appearsEnabled) {
        stateChanger.setEnabled(appearsEnabled);
    }

    @Override
    protected void onClick() {
        if (stateChanger.isEnabled()) {
            super.onClick();
        }
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        boolean viewEnabled = isEnabled() && stateChanger.isEnabled();
        stateChanger.enableView(view, viewEnabled);
    }
}

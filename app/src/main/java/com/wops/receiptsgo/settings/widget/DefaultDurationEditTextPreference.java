package com.wops.receiptsgo.settings.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.wops.receiptsgo.R;
import wb.android.preferences.IntegerSummaryEditTextPreference;

public class DefaultDurationEditTextPreference extends IntegerSummaryEditTextPreference {

	public DefaultDurationEditTextPreference(Context context) {
		super(context);
	}

	public DefaultDurationEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DefaultDurationEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public CharSequence getSummary() {
		if (TextUtils.isEmpty(getText())) {
			return "";
		}
		else {
			try {
				int days = Integer.parseInt(getText());
				if (days == 1) {
					return getContext().getString(R.string.day, Integer.toString(days));
				}
				else {
					return getContext().getString(R.string.days, Integer.toString(days));
				}
			} catch (NumberFormatException e) {
				return "";
			}
		}
	}

}

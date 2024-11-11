package com.wops.receiptsgo.settings.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.text.DecimalFormat;

import wb.android.preferences.FloatSummaryEditTextPreference;

public class DefaultTaxPercentagePreference extends FloatSummaryEditTextPreference {

	public DefaultTaxPercentagePreference(Context context) {
		super(context);
	}

	public DefaultTaxPercentagePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DefaultTaxPercentagePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public CharSequence getSummary() {
		if (TextUtils.isEmpty(getText())) { //Add a zero check
			return "";
		}
		else {
			try {
				float value = Float.parseFloat(getText());
				if (value <= 0) {
					return "";
				}
				DecimalFormat decimalFormat = new DecimalFormat();
				decimalFormat.setMaximumFractionDigits(2);
				decimalFormat.setMinimumFractionDigits(2);
				decimalFormat.setGroupingUsed(false);
				return decimalFormat.format(value) + "%";
			} catch (NumberFormatException e) {
				return "";
			}
		}
	}
}

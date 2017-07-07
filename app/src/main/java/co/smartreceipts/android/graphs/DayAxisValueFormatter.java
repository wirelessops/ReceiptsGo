package co.smartreceipts.android.graphs;

import android.text.format.DateFormat;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DayAxisValueFormatter implements IAxisValueFormatter {

    public DayAxisValueFormatter() {
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        try { // Hack for sometimes appearing IndexOutOfBoundsException from MPAndroidCharts lib
            int days = (int) value;

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_YEAR, days);

            String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMdd");

            return new SimpleDateFormat(pattern, Locale.getDefault()).format(calendar.getTime());

        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

}

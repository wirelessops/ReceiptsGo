package co.smartreceipts.android.graphs;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Calendar;

public class DayAxisValueFormatter implements IAxisValueFormatter
{
//    private final ILineDataSet dataSet;
    private final String defaultSeparator;

    public DayAxisValueFormatter(/*ILineDataSet dataSet, */String dafaultSeparator) {
        //this.dataSet = dataSet;
        this.defaultSeparator = dafaultSeparator;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

//        if (value >= dataSet.getXMin() + dataSet.getEntryCount()) {
//            Logger.debug(this, "possible IndexOutOfBoundsException");
//            return "";
//        }

//        int days = (int) dataSet.getEntryForXValue(value, 0).getX(); //getEntryXPos(float value).getData();

        try {
            int days = (int) value;

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_YEAR, days);

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;

            return String.format("%02d", day) + defaultSeparator + String.format("%02d", month);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

}

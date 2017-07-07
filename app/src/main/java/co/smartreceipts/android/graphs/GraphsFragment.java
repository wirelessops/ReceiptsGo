package co.smartreceipts.android.graphs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BaseEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.ReportInfoFragment;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.graphs.entry.LabeledGraphEntry;
import co.smartreceipts.android.model.Trip;
import dagger.android.support.AndroidSupportInjection;

public class GraphsFragment extends WBFragment implements GraphsView {

    private static final int[] GRAPHS_PALETTE = {
            R.color.graph_1, R.color.graph_2, R.color.graph_3, R.color.graph_4,
            R.color.graph_5, R.color.graph_6, R.color.graph_7
    };

    private static final float TITLE_TEXT_SIZE = 14f;
    private static final float VALUE_TEXT_SIZE = 12f;
    private static final float LEGEND_TEXT_SIZE = 12f;
    private static final int ANIMATION_DURATION = 2000;
    private static final float EXTRA_TOP_OFFSET_NORMAL = 25f;
    private static final float EXTRA_TOP_OFFSET_SMALL = 10f;


    @BindView(R.id.empty_text)
    TextView emptyText;

    @BindView(R.id.dates_line_chart)
    LineChart datesLineChart;

    @BindView(R.id.categories_pie_chart)
    PieChart categoriesPieChart;

    @BindView(R.id.reimbursable_horizontal_bar_chart)
    HorizontalBarChart reimbursableBarChart;

    @BindView(R.id.payment_methods_bar_chart)
    BarChart paymentMethodsBarChart;

    @Inject
    GraphsPresenter presenter;

    private Unbinder unbinder;
    private final IValueFormatter valueFormatter = new DefaultValueFormatter(1);

    @NonNull
    public static GraphsFragment newInstance() {
        return new GraphsFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.graphs_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.unbinder = ButterKnife.bind(this, view);

        initDatesLineChart();
        initCategoriesPieChart();
        initReimbursableBarChart();
        initPaymentMethodsBarChart();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.subscribe(getTrip());
    }

    @Override
    public void onPause() {
        presenter.unsubscribe();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        this.unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void showEmptyText(boolean visible) {
        emptyText.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void present(GraphUiIndicator uiIndicator) {
        emptyText.setVisibility(View.GONE);

        switch (uiIndicator.getGraphType()) {
            case SummationByDate:
                showSummationByDate(uiIndicator.getEntries());
                datesLineChart.setVisibility(View.VISIBLE);
                break;
            case SummationByCategory:
                showSummationByCategory(uiIndicator.getEntries());
                categoriesPieChart.setVisibility(View.VISIBLE);
                break;
            case SummationByReimbursment:
                showSummationByReimbursment(uiIndicator.getEntries());
                reimbursableBarChart.setVisibility(View.VISIBLE);
                break;
            case SummationByPaymentMethod:
                showPaymentMethodsBarChart(uiIndicator.getEntries());
                paymentMethodsBarChart.setVisibility(View.VISIBLE);
                break;
            default:
                throw new IllegalStateException("Unknown graph type!");
        }
    }

    private void showSummationByDate(List<? extends BaseEntry> entries) {
        datesLineChart.post(() -> setDescription(datesLineChart, R.string.graphs_expenditure_by_dates_title));

        ArrayList<Entry> lineEntries = new ArrayList<>();

        for (BaseEntry entry : entries) {
            Entry GraphEntry = (Entry) entry;
            lineEntries.add(new Entry(GraphEntry.getX(), GraphEntry.getY()));
        }

        LineDataSet dataSet = new LineDataSet(lineEntries, "");
        dataSet.setColor(ContextCompat.getColor(getContext(), GRAPHS_PALETTE[2]));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(VALUE_TEXT_SIZE);
        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
            if (value > 0) {
                return String.valueOf((int) value);
            }
            return "";
        });
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setLineWidth(3f);

        datesLineChart.setData(new LineData(dataSet));

        // animate without any Easing because of strange MPAndroidChart's bug with IndexOutOfBoundsException
        datesLineChart.animateX(ANIMATION_DURATION);
    }

    private void showSummationByCategory(List<? extends BaseEntry> entries) {
        categoriesPieChart.post(() -> setDescription(categoriesPieChart, R.string.graphs_expenditure_by_categories_title));

        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        for (BaseEntry graphEntry : entries) {
            pieEntries.add(new PieEntry(graphEntry.getY(), ((LabeledGraphEntry) graphEntry).getLabel()));
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(GRAPHS_PALETTE, getContext());
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(VALUE_TEXT_SIZE);
        dataSet.setValueFormatter(valueFormatter);

        dataSet.setValueLinePart1OffsetPercentage(70.f);
        dataSet.setValueLineColor(Color.WHITE);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        categoriesPieChart.setData(new PieData(dataSet));

        categoriesPieChart.animateY(ANIMATION_DURATION, Easing.EasingOption.EaseOutBack);
    }

    private void showSummationByReimbursment(List<? extends BaseEntry> entries) {
        reimbursableBarChart.post(() -> setDescription(reimbursableBarChart, R.string.graphs_expenditure_by_reimbursable_title));

        String[] labels = new String[2];
        float[] values = new float[2];

        for (int i = 0; i < entries.size(); i++) {
            labels[i] = ((LabeledGraphEntry) entries.get(i)).getLabel();
            values[i] = entries.get(i).getY();
        }

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, values));

        BarDataSet dataSet = new BarDataSet(barEntries, "");
        dataSet.setDrawIcons(false);
        dataSet.setValueTextSize(VALUE_TEXT_SIZE);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSet.setColors(new int[]{R.color.graph_7, R.color.graph_2}, getContext());
        dataSet.setStackLabels(labels);
        dataSet.setValueFormatter(valueFormatter);

        reimbursableBarChart.setData(new BarData(dataSet));

        reimbursableBarChart.animateY(ANIMATION_DURATION, Easing.EasingOption.EaseOutBack);
    }

    private void showPaymentMethodsBarChart(List<? extends BaseEntry> entries) {
        paymentMethodsBarChart.post(() -> setDescription(paymentMethodsBarChart, R.string.graphs_expenditure_by_payment_methods_title));

        List<IBarDataSet> sets = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            LabeledGraphEntry graphEntry = (LabeledGraphEntry) entries.get(i);

            BarDataSet verticalSet = new BarDataSet(Collections.singletonList(new BarEntry(i, graphEntry.getY())), graphEntry.getLabel());
            verticalSet.setValueTextColor(Color.WHITE);
            verticalSet.setValueTextSize(VALUE_TEXT_SIZE);
            verticalSet.setColor(ContextCompat.getColor(getContext(), GRAPHS_PALETTE[i]));
            verticalSet.setValueFormatter(valueFormatter);

            sets.add(verticalSet);
        }

        paymentMethodsBarChart.setData(new BarData(sets));

        paymentMethodsBarChart.animateY(ANIMATION_DURATION, Easing.EasingOption.EaseOutBack);
    }

    private void initDatesLineChart() {
        datesLineChart.setDrawGridBackground(false);
        datesLineChart.getLegend().setEnabled(false);

        XAxis xAxis = datesLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setValueFormatter(new DayAxisValueFormatter());

        datesLineChart.setClickable(false);
        datesLineChart.setExtraTopOffset(EXTRA_TOP_OFFSET_NORMAL);
    }

    private void initCategoriesPieChart() {
        categoriesPieChart.setCenterText(getTrip().getName());
        categoriesPieChart.setCenterTextColor(Color.WHITE);
        categoriesPieChart.setHoleColor(Color.TRANSPARENT);
        categoriesPieChart.setEntryLabelTextSize(VALUE_TEXT_SIZE);

        categoriesPieChart.setHoleRadius(35f);
        categoriesPieChart.setTransparentCircleRadius(40f);

        categoriesPieChart.getLegend().setEnabled(false);

        categoriesPieChart.setExtraTopOffset(EXTRA_TOP_OFFSET_SMALL);
    }

    private void initReimbursableBarChart() {
        reimbursableBarChart.setTouchEnabled(false);

        reimbursableBarChart.setDrawGridBackground(false);
        reimbursableBarChart.setDrawValueAboveBar(false);

        reimbursableBarChart.getXAxis().setEnabled(false);
        reimbursableBarChart.getAxisRight().setEnabled(false);
        reimbursableBarChart.getAxisLeft().setEnabled(false);

        setDefaultLegend(reimbursableBarChart);

        reimbursableBarChart.setExtraTopOffset(EXTRA_TOP_OFFSET_NORMAL);
    }

    private void initPaymentMethodsBarChart() {
        paymentMethodsBarChart.setTouchEnabled(false);

        paymentMethodsBarChart.setFitBars(true);

        paymentMethodsBarChart.getXAxis().setEnabled(false);
        paymentMethodsBarChart.getAxisRight().setEnabled(false);
        paymentMethodsBarChart.getAxisLeft().setEnabled(false);

        setDefaultLegend(paymentMethodsBarChart);

        paymentMethodsBarChart.setExtraTopOffset(EXTRA_TOP_OFFSET_NORMAL);
    }

    private Trip getTrip() {
        return ((ReportInfoFragment) getParentFragment()).getTrip();
    }

    private void setDefaultLegend(Chart chart) {
        Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(LEGEND_TEXT_SIZE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
    }

    private void setDescription(Chart chart, int stringId) {
        Description description = chart.getDescription();
        description.setText(getResources().getString(stringId));
        description.setTextColor(Color.WHITE);
        description.setTextAlign(Paint.Align.CENTER);
        description.setPosition(chart.getWidth() / 2, VALUE_TEXT_SIZE * 2.5f);
        description.setTextSize(TITLE_TEXT_SIZE);
    }

}

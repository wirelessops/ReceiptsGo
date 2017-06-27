package co.smartreceipts.android.graphs;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import dagger.android.support.AndroidSupportInjection;

public class GraphsFragment extends WBFragment implements GraphsView {

    private static final int[] GRAPHS_PALETTE = {
            R.color.graph_1, R.color.graph_2, R.color.graph_3, R.color.graph_4,
            R.color.graph_5, R.color.graph_6, R.color.graph_red, R.color.graph_green
    };

    public static final float VALUE_TEXT_SIZE = 12f;
    public static final float LEGEND_TEXT_SIZE = 12f;
    public static final int ANIMATION_DURATION = 2000;

    // TODO: 25.06.2017 проверить, можно ли по-умному всё-таки вставить заголовок графика

    @BindView(R.id.empty_text)
    TextView emptyText;

    @BindView(R.id.dates_line_chart)
    LineChart datesLineChart;
    @BindView(R.id.dates_line_chart_title)
    TextView datesLineChartTitle;

    @BindView(R.id.categories_pie_chart)
    PieChart categoriesPieChart;
    @BindView(R.id.categories_pie_chart_title)
    TextView categoriesPieChartTitle;

    @BindView(R.id.reimbursable_horizontal_bar_chart)
    HorizontalBarChart reimbursableBarChart;
    @BindView(R.id.reimbursable_horizontal_bar_chart_title)
    TextView reimbursableBarChartTitle;

    @BindView(R.id.payment_methods_bar_chart)
    BarChart paymentMethodsBarChart;
    @BindView(R.id.payment_methods_bar_chart_title)
    TextView paymentMethodsBarChartTitle;

    @Inject
    GraphsPresenter presenter;

    @Inject
    UserPreferenceManager preferenceManager;

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

//        animateGraphs();
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
                datesLineChart.setVisibility(View.VISIBLE);
                datesLineChartTitle.setVisibility(View.VISIBLE);
                showSummationByDate(uiIndicator.getEntries());
                break;
            case SummationByCategory:
                categoriesPieChart.setVisibility(View.VISIBLE);
                categoriesPieChartTitle.setVisibility(View.VISIBLE);
                showSummationByCategory(uiIndicator.getEntries());
                break;
            case SummationByReimbursment:
                reimbursableBarChart.setVisibility(View.VISIBLE);
                reimbursableBarChartTitle.setVisibility(View.VISIBLE);
                showSummationByReimbursment(uiIndicator.getEntries());
                break;
            case SummationByPaymentMethod:
                paymentMethodsBarChart.setVisibility(View.VISIBLE);
                paymentMethodsBarChartTitle.setVisibility((View.VISIBLE));
                showPaymentMethodsBarChart(uiIndicator.getEntries());
                break;
            default:
                throw new IllegalStateException("Unknown graph type!");
        }
    }

    private void showSummationByDate(List<? extends BaseEntry> entries) {
        ArrayList<Entry> lineEntries = new ArrayList<>();

        for (BaseEntry entry : entries) {
            Entry GraphEntry = (Entry) entry;
            lineEntries.add(new Entry(GraphEntry.getX(), GraphEntry.getY()));
        }

        LineDataSet dataSet = new LineDataSet(lineEntries, "");
//        dataSet.setColors(GRAPHS_PALETTE, getContext());
        dataSet.setColor(getResources().getColor(GRAPHS_PALETTE[2]));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(VALUE_TEXT_SIZE);
//        dataSet.setValueFormatter(valueFormatter);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setLineWidth(3f);

        datesLineChart.setData(new LineData(dataSet));
        datesLineChart.invalidate();

        // no animation because of strange lib's IndexOutOfBoundsException
//        datesLineChart.animateX(ANIMATION_DURATION, Easing.EasingOption.EaseOutBack);
    }

    private void showSummationByCategory(List<? extends BaseEntry> entries) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        for (BaseEntry graphEntry : entries) {
            pieEntries.add(new PieEntry(graphEntry.getY(), ((LabeledGraphEntry) graphEntry).getLabel()));
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "Label");
        dataSet.setColors(GRAPHS_PALETTE, getContext());
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(VALUE_TEXT_SIZE);
        dataSet.setValueFormatter(valueFormatter);

        categoriesPieChart.setData(new PieData(dataSet));
        categoriesPieChart.invalidate();

        categoriesPieChart.animateY(ANIMATION_DURATION, Easing.EasingOption.EaseOutBack);
    }

    private void showSummationByReimbursment(List<? extends BaseEntry> entries) {
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
        dataSet.setColors(new int[]{R.color.graph_red, R.color.graph_green}, getContext());
//        dataSet.setColors(GRAPHS_PALETTE, getContext());
        dataSet.setStackLabels(labels);
        dataSet.setValueFormatter(valueFormatter);

        reimbursableBarChart.setData(new BarData(dataSet));
        reimbursableBarChart.invalidate();

        reimbursableBarChart.animateY(ANIMATION_DURATION, Easing.EasingOption.EaseOutBack);
    }

    private void showPaymentMethodsBarChart(List<? extends BaseEntry> entries) {
        List<IBarDataSet> sets = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            LabeledGraphEntry graphEntry = (LabeledGraphEntry) entries.get(i);

            BarDataSet verticalSet = new BarDataSet(Collections.singletonList(new BarEntry(i, graphEntry.getY())), graphEntry.getLabel());
            verticalSet.setValueTextColor(Color.WHITE);
            verticalSet.setValueTextSize(VALUE_TEXT_SIZE);
            verticalSet.setColor(getResources().getColor(GRAPHS_PALETTE[i]));
            verticalSet.setValueFormatter(valueFormatter);

            sets.add(verticalSet);
        }

        paymentMethodsBarChart.setData(new BarData(sets));
        paymentMethodsBarChart.invalidate();

        paymentMethodsBarChart.animateY(ANIMATION_DURATION, Easing.EasingOption.EaseOutBack);
    }

    private void initDatesLineChart() {

//        setChartNotClickable(datesLineChart);

        datesLineChart.setDrawGridBackground(false);
        datesLineChart.getLegend().setEnabled(false);
        datesLineChart.getDescription().setEnabled(false);

        XAxis xAxis = datesLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);

        xAxis.setValueFormatter(new DayAxisValueFormatter(preferenceManager.get(UserPreference.General.DateSeparator)));
    }

    private void initCategoriesPieChart() {
        categoriesPieChart.setCenterText(getTrip().getName());
        categoriesPieChart.setCenterTextColor(Color.WHITE);
        categoriesPieChart.setHoleColor(Color.TRANSPARENT);
        categoriesPieChart.setEntryLabelTextSize(VALUE_TEXT_SIZE);

        categoriesPieChart.getLegend().setEnabled(false);
    }

    private void initReimbursableBarChart() {

        reimbursableBarChart.setDrawGridBackground(false);
        reimbursableBarChart.setDrawValueAboveBar(false);

        setChartNotClickable(reimbursableBarChart);

        reimbursableBarChart.getDescription().setEnabled(false);

        reimbursableBarChart.getXAxis().setEnabled(false);
        reimbursableBarChart.getAxisRight().setEnabled(false);
        reimbursableBarChart.getAxisLeft().setEnabled(false);

//        reimbursableBarChart.setFitBars(true);

        Legend legend = reimbursableBarChart.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(LEGEND_TEXT_SIZE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
    }

    private void initPaymentMethodsBarChart() {

        setChartNotClickable(paymentMethodsBarChart);

        paymentMethodsBarChart.getDescription().setEnabled(false);
        paymentMethodsBarChart.setFitBars(true);

        paymentMethodsBarChart.getXAxis().setEnabled(false);
        paymentMethodsBarChart.getAxisRight().setEnabled(false);
        paymentMethodsBarChart.getAxisLeft().setEnabled(false);

        Legend legend = paymentMethodsBarChart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(LEGEND_TEXT_SIZE);
        legend.setDrawInside(false);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
    }

    private Trip getTrip() {
        return ((ReportInfoFragment) getParentFragment()).getTrip();
    }

    private void setChartNotClickable(Chart chart) {
        chart.setClickable(false);
        chart.setTouchEnabled(false);
    }

}

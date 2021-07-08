package co.smartreceipts.android.graphs;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.Chart;
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

import co.smartreceipts.android.R;
import co.smartreceipts.android.databinding.GraphsFragmentBinding;
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

    private static final float VALUE_TEXT_SIZE = 12f;
    private static final float LEGEND_TEXT_SIZE = 12f;
    private static final int ANIMATION_DURATION = 2000;
    private static final float EXTRA_OFFSET_NORMAL = 16f;

    @Inject
    GraphsPresenter presenter;

    private GraphsFragmentBinding binding;
    private boolean isGraphPresenterSubscribed = false;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = GraphsFragmentBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initDatesLineChart();
        initCategoriesPieChart();
        initReimbursableBarChart();
        initPaymentMethodsBarChart();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(null);
        }

        if (presenter != null) {
            if (isVisibleToUser && isResumed() && !isGraphPresenterSubscribed) {
                // Unlike normal situations, we only subscribe this one when it's actually visible
                // Since the graphs are somewhat slow to load. This speeds up the rendering process
                isGraphPresenterSubscribed = true;
                presenter.subscribe(getTrip());
            }
        }
    }

    @Override
    public void onPause() {
        presenter.unsubscribe();
        isGraphPresenterSubscribed = false;
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void showEmptyText(boolean visible) {
        binding.noData.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            binding.progress.setVisibility(View.GONE);
        }
    }

    @Override
    public void present(GraphUiIndicator uiIndicator) {
        binding.noData.setVisibility(View.GONE);
        binding.progress.setVisibility(View.GONE);

        switch (uiIndicator.getGraphType()) {
            case SummationByDate:
                showSummationByDate(uiIndicator.getEntries());
                binding.datesLineChart.setVisibility(View.VISIBLE);
                break;
            case SummationByCategory:
                showSummationByCategory(uiIndicator.getEntries());
                binding.categoriesPieChart.setVisibility(View.VISIBLE);
                break;
            case SummationByReimbursement:
                showSummationByReimbursement(uiIndicator.getEntries());
                binding.reimbursableHorizontalBarChart.setVisibility(View.VISIBLE);
                break;
            case SummationByPaymentMethod:
                showPaymentMethodsBarChart(uiIndicator.getEntries());
                binding.paymentMethodsBarChart.setVisibility(View.VISIBLE);
                break;
            default:
                throw new IllegalStateException("Unknown graph type!");
        }
    }

    private void showSummationByDate(List<? extends BaseEntry> entries) {
        binding.datesTitle.setVisibility(View.VISIBLE);
        binding.datesLineChart.post(() -> hideDescription(binding.datesLineChart));

        ArrayList<Entry> lineEntries = new ArrayList<>();

        for (BaseEntry entry : entries) {
            Entry GraphEntry = (Entry) entry;
            lineEntries.add(new Entry(GraphEntry.getX(), GraphEntry.getY()));
        }

        LineDataSet dataSet = new LineDataSet(lineEntries, "");
        dataSet.setColor(ContextCompat.getColor(getContext(),R.color.graph_2));
        dataSet.setCircleColors(new int[]{R.color.graph_3}, getContext());
        dataSet.setValueTextSize(VALUE_TEXT_SIZE);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_color));
        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> (value > 0) ? String.valueOf((int) value) : "");
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setLineWidth(3f);

        binding.datesLineChart.setData(new LineData(dataSet));

        // animate without any Easing because of strange MPAndroidChart's bug with IndexOutOfBoundsException
        binding.datesLineChart.animateX(ANIMATION_DURATION);
    }

    private void showSummationByCategory(List<? extends BaseEntry> entries) {
        binding.categoriesTitle.setVisibility(View.VISIBLE);
        binding.categoriesPieChart.post(() -> hideDescription(binding.categoriesPieChart));

        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        for (BaseEntry graphEntry : entries) {
            pieEntries.add(new PieEntry(graphEntry.getY(), ((LabeledGraphEntry) graphEntry).getLabel()));
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(GRAPHS_PALETTE, getContext());
        dataSet.setSliceSpace(1f);
        dataSet.setValueTextSize(VALUE_TEXT_SIZE);
        dataSet.setValueFormatter(valueFormatter);

        dataSet.setValueLineColor(ContextCompat.getColor(getContext(), R.color.transparent_overlay));
        dataSet.setValueLinePart1OffsetPercentage(70.f);
        dataSet.setValueLinePart1Length(0.5f);
        dataSet.setValueLinePart2Length(0.25f);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        binding.categoriesPieChart.setData(new PieData(dataSet));

        binding.categoriesPieChart.animateY(ANIMATION_DURATION, Easing.EaseOutBack);
    }

    private void showSummationByReimbursement(List<? extends BaseEntry> entries) {
        binding.reimbursableTitle.setVisibility(View.VISIBLE);
        binding.reimbursableHorizontalBarChart.post(() -> hideDescription(binding.reimbursableHorizontalBarChart));

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
        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSet.setColors(new int[]{R.color.graph_2, R.color.graph_3}, getContext());
        dataSet.setStackLabels(labels);
        dataSet.setValueFormatter(valueFormatter);

        binding.reimbursableHorizontalBarChart.setData(new BarData(dataSet));

        binding.reimbursableHorizontalBarChart.animateY(ANIMATION_DURATION, Easing.EaseOutBack);
    }

    private void showPaymentMethodsBarChart(List<? extends BaseEntry> entries) {
        binding.paymentMethodsTitle.setVisibility(View.VISIBLE);
        binding.paymentMethodsBarChart.post(() -> hideDescription(binding.paymentMethodsBarChart));

        List<IBarDataSet> sets = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            LabeledGraphEntry graphEntry = (LabeledGraphEntry) entries.get(i);

            BarDataSet verticalSet = new BarDataSet(Collections.singletonList(new BarEntry(i, graphEntry.getY())), graphEntry.getLabel());
            verticalSet.setValueTextSize(VALUE_TEXT_SIZE);
            verticalSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_color));
            verticalSet.setColor(ContextCompat.getColor(getContext(), GRAPHS_PALETTE[i]));
            verticalSet.setValueFormatter(valueFormatter);

            sets.add(verticalSet);
        }

        binding.paymentMethodsBarChart.setData(new BarData(sets));

        binding.paymentMethodsBarChart.animateY(ANIMATION_DURATION, Easing.EaseOutBack);
    }

    private void initDatesLineChart() {
        int labelColor = ContextCompat.getColor(requireContext(), R.color.text_primary_color);

        binding.datesLineChart.setDrawGridBackground(false);
        binding.datesLineChart.getLegend().setEnabled(false);

        XAxis xAxis = binding.datesLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new DayAxisValueFormatter());
        xAxis.setTextColor(labelColor);

        binding.datesLineChart.getAxisLeft().setTextColor(labelColor);
        binding.datesLineChart.getAxisRight().setTextColor(labelColor);

        binding.datesLineChart.setClickable(false);
        binding.datesLineChart.setExtraOffsets(EXTRA_OFFSET_NORMAL, EXTRA_OFFSET_NORMAL, EXTRA_OFFSET_NORMAL, EXTRA_OFFSET_NORMAL);
    }

    private void initCategoriesPieChart() {
        int labelColor = ContextCompat.getColor(requireContext(), R.color.text_primary_color);

        binding.categoriesPieChart.setEntryLabelTextSize(VALUE_TEXT_SIZE);
        binding.categoriesPieChart.setEntryLabelColor(labelColor);

        binding.categoriesPieChart.setCenterText(getTrip().getName());
        binding.categoriesPieChart.setCenterTextColor(labelColor);
        binding.categoriesPieChart.setHoleColor(Color.TRANSPARENT);
        binding.categoriesPieChart.setHoleRadius(35f);
        binding.categoriesPieChart.setTransparentCircleRadius(40f);

        binding.categoriesPieChart.getLegend().setEnabled(false);

        binding.categoriesPieChart.setExtraOffsets(EXTRA_OFFSET_NORMAL, EXTRA_OFFSET_NORMAL, EXTRA_OFFSET_NORMAL, EXTRA_OFFSET_NORMAL);
    }

    private void initReimbursableBarChart() {
        binding.reimbursableHorizontalBarChart.setTouchEnabled(false);

        binding.reimbursableHorizontalBarChart.setDrawGridBackground(false);
        binding.reimbursableHorizontalBarChart.setDrawValueAboveBar(false);

        binding.reimbursableHorizontalBarChart.getXAxis().setEnabled(false);
        binding.reimbursableHorizontalBarChart.getAxisRight().setEnabled(false);
        binding.reimbursableHorizontalBarChart.getAxisLeft().setEnabled(false);

        setDefaultLegend(binding.reimbursableHorizontalBarChart);

        binding.reimbursableHorizontalBarChart.setExtraOffsets(EXTRA_OFFSET_NORMAL, EXTRA_OFFSET_NORMAL, EXTRA_OFFSET_NORMAL, EXTRA_OFFSET_NORMAL);
    }

    private void initPaymentMethodsBarChart() {
        binding.paymentMethodsBarChart.setTouchEnabled(false);

        binding.paymentMethodsBarChart.setFitBars(true);

        binding.paymentMethodsBarChart.getXAxis().setEnabled(false);
        binding.paymentMethodsBarChart.getAxisRight().setEnabled(false);
        binding.paymentMethodsBarChart.getAxisLeft().setEnabled(false);

        setDefaultLegend(binding.paymentMethodsBarChart);

        // double top offset because values are located above the bars
        binding.paymentMethodsBarChart.setExtraOffsets(EXTRA_OFFSET_NORMAL, EXTRA_OFFSET_NORMAL * 2, EXTRA_OFFSET_NORMAL, EXTRA_OFFSET_NORMAL);
    }

    private Trip getTrip() {
        return ((ReportInfoFragment) getParentFragment()).getTrip();
    }

    private void setDefaultLegend(Chart chart) {
        Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setTextSize(LEGEND_TEXT_SIZE);
        legend.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_color));
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setXEntrySpace(EXTRA_OFFSET_NORMAL);
        legend.setYOffset(EXTRA_OFFSET_NORMAL);
    }

    private void hideDescription(Chart chart) {
        //We're disabling default description in favor of custom-formatted text views
        if (chart != null) {
            Description description = chart.getDescription();
            description.setText("");
            description.setTextSize(0);
        }
    }

}

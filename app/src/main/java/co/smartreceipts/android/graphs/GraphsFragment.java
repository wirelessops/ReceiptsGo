package co.smartreceipts.android.graphs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.ReportInfoFragment;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.controllers.grouping.GroupingController;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class GraphsFragment extends WBFragment {

    @BindView(R.id.test_text)
    TextView testTextView;

    @Inject
    GroupingController groupingController;
    @Inject
    UserPreferenceManager preferenceManager;

    private Unbinder unbinder;

    private Trip trip;

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
        return inflater.inflate(R.layout.graphs_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.unbinder = ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        trip = ((ReportInfoFragment) getParentFragment()).getTrip();
    }

    @Override
    public void onResume() {
        super.onResume();

//        groupingController.getReceiptsGroupedByCategory(trip)
//                .observeOn(AndroidSchedulers.mainThread()) // this should be in interactor
//                .subscribe(categoryGroupingResult -> {
//                    String str = "\n" + categoryGroupingResult.getCategory().getName() + " receipts count = " + categoryGroupingResult.getReceipts().size();
//                    testTextView.append(str);
//                });

        groupingController.getSummationByCategory(trip)
                .observeOn(AndroidSchedulers.mainThread()) // this should be in interactor
                .subscribe(sumCategoryGroupingResult -> {
                    Category category = sumCategoryGroupingResult.getCategory();
                    String str = "\n    Category = " + category.getName() + "\nCode = " + category.getCode() +
                            "\nPrice = " + sumCategoryGroupingResult.getPrice() +
                            "\nTax = " + sumCategoryGroupingResult.getTax() +
                            "\nCurrency = " + sumCategoryGroupingResult.getCurrency().getCurrencyCode() + "\n";
                    testTextView.append(str);
                });

        if (preferenceManager.get(UserPreference.Receipts.UsePaymentMethods)) {
            groupingController.getSummationByPaymentMethod(trip)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(throwable -> testTextView.append("\n" + throwable.getMessage() + "\n"))
                    .subscribe(paymentMethodGroupingResult -> {
                        PaymentMethod paymentMethod = paymentMethodGroupingResult.getPaymentMethod();

                        String str = "\n    Payment method = " + paymentMethod.getMethod() +
                                "\nPrice = " + paymentMethodGroupingResult.getPrice() + "\n";

                        testTextView.append(str);
                    });
        }


    }

    @Override
    public void onDestroyView() {
        this.unbinder.unbind();
        super.onDestroyView();
    }
}

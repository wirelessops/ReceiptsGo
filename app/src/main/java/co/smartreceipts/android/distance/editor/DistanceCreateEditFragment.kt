package co.smartreceipts.android.distance.editor

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import butterknife.BindViews
import butterknife.ButterKnife
import butterknife.ViewCollections
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import co.smartreceipts.android.R
import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.adapters.FooterButtonArrayAdapter
import co.smartreceipts.android.autocomplete.AutoCompleteArrayAdapter
import co.smartreceipts.android.autocomplete.AutoCompleteField
import co.smartreceipts.android.autocomplete.AutoCompleteResult
import co.smartreceipts.android.autocomplete.distance.DistanceAutoCompleteField
import co.smartreceipts.android.currency.widget.CurrencyListEditorPresenter
import co.smartreceipts.android.currency.widget.DefaultCurrencyListEditorView
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.distance.editor.currency.DistanceCurrencyCodeSupplier
import co.smartreceipts.android.fragments.WBFragment
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.PaymentMethod
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.model.factory.DistanceBuilderFactory
import co.smartreceipts.android.model.utils.ModelUtils
import co.smartreceipts.android.persistence.DatabaseHelper
import co.smartreceipts.android.receipts.editor.paymentmethods.PaymentMethodsPresenter
import co.smartreceipts.android.receipts.editor.paymentmethods.PaymentMethodsView
import co.smartreceipts.android.utils.SoftKeyboardManager
import co.smartreceipts.android.utils.butterknife.ButterKnifeActions
import co.smartreceipts.android.widget.model.UiIndicator
import com.jakewharton.rxbinding3.widget.textChanges
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.update_distance.*
import java.math.BigDecimal
import java.sql.Date
import java.util.*
import javax.inject.Inject

class DistanceCreateEditFragment : WBFragment(), DistanceCreateEditView, View.OnFocusChangeListener, PaymentMethodsView {
    @Inject
    lateinit var presenter: DistanceCreateEditPresenter

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var database: DatabaseHelper

    @Inject
    lateinit var dateFormatter: DateFormatter

    @Inject
    lateinit var navigationHandler: NavigationHandler<SmartReceiptsActivity>

    @Inject
    lateinit var paymentMethodsPresenter: PaymentMethodsPresenter

    @BindViews(R.id.distance_input_guide_image_payment_method, R.id.distance_input_payment_method)
    lateinit var paymentMethodsViewsList: List<@JvmSuppressWildcards View>

    override val editableItem: Distance?
        get() = arguments?.getParcelable(Distance.PARCEL_KEY)

    private val parentTrip: Trip
        get() = arguments?.getParcelable(Trip.PARCEL_KEY) ?: throw IllegalStateException("Distance can't exist without parent trip")

    private var suggestedDate: Date = Date(Calendar.getInstance().timeInMillis)

    private lateinit var currencyListEditorPresenter: CurrencyListEditorPresenter

    private var focusedView: View? = null

    private lateinit var paymentMethodsAdapter: FooterButtonArrayAdapter<PaymentMethod>

    override val createDistanceClicks: Observable<Distance>
        get() = _createDistanceClicks

    override val updateDistanceClicks: Observable<Distance>
        get() = _updateDistanceClicks

    override val deleteDistanceClicks: Observable<Distance>
        get() = _deleteDistanceClicks

    private val _createDistanceClicks: Subject<Distance> = PublishSubject.create<Distance>().toSerialized()
    private val _updateDistanceClicks: Subject<Distance> = PublishSubject.create<Distance>().toSerialized()
    private val _deleteDistanceClicks: Subject<Distance> = PublishSubject.create<Distance>().toSerialized()


    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        focusedView = if (hasFocus) view else null
        if (editableItem == null && hasFocus) {
            // Only launch if we have focus and it's a new distance
            SoftKeyboardManager.showKeyboard(view)
        }
    }

    override fun onResume() {
        super.onResume()
        focusedView?.requestFocus() // Make sure we're focused on the right view
    }

    override fun onPause() {
        SoftKeyboardManager.hideKeyboard(focusedView)
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            suggestedDate = Date(arguments?.getLong(ARG_SUGGESTED_DATE, suggestedDate.time) ?: suggestedDate.time)
        }

        paymentMethodsAdapter = FooterButtonArrayAdapter(requireActivity(), ArrayList(),
                R.string.manage_payment_methods) {
            analytics.record(Events.Informational.ClickedManagePaymentMethods)
            navigationHandler.navigateToPaymentMethodsEditor()
        }

        currencyListEditorPresenter =
            CurrencyListEditorPresenter(
                DefaultCurrencyListEditorView(requireContext()) { spinner_currency },
                database,
                DistanceCurrencyCodeSupplier(parentTrip, editableItem),
                savedInstanceState
            )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ourView: View = inflater.inflate(R.layout.update_distance, container, false)
        ButterKnife.bind(this, ourView)
        return ourView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFocusBehavior()

        // Toolbar stuff
        when {
            navigationHandler.isDualPane -> toolbar.visibility = View.GONE
            else -> setSupportActionBar(toolbar as Toolbar)
        }

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_clear_24dp)
            setTitle(if (editableItem == null) R.string.dialog_mileage_title_create else R.string.dialog_mileage_title_update)
            subtitle = ""
        }


        if (editableItem == null) {
            // New Distance
            text_distance_date.date = suggestedDate
            text_distance_rate.setText(presenter.getDefaultDistanceRate())
        } else {
            // Update distance
            text_distance_value.setText(editableItem!!.decimalFormattedDistance)
            text_distance_rate.setText(editableItem!!.decimalFormattedRate)
            text_distance_location.setText(editableItem!!.location)
            text_distance_comment.setText(editableItem!!.comment)
            text_distance_date.date = editableItem!!.date
            text_distance_date.timeZone = editableItem!!.timeZone
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.subscribe()
        currencyListEditorPresenter.subscribe()
        paymentMethodsPresenter.subscribe()
    }

    override fun onStop() {
        presenter.unsubscribe()
        currencyListEditorPresenter.unsubscribe()
        paymentMethodsPresenter.unsubscribe()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currencyListEditorPresenter.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(if (editableItem == null) R.menu.menu_save else R.menu.menu_save_delete, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                navigationHandler.navigateBack()
                return true
            }
            R.id.action_save -> {
                when {
                    editableItem != null -> _updateDistanceClicks.onNext(constructDistance())
                    else -> _createDistanceClicks.onNext(constructDistance())
                }
                return true
            }
            R.id.action_delete -> {
                showDeleteDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun present(uiIndicator: UiIndicator<Int>) {
        when (uiIndicator.state) {
            UiIndicator.State.Success -> navigationHandler.navigateBack()
            else -> if (uiIndicator.state == UiIndicator.State.Error && uiIndicator.data.isPresent) {
                Toast.makeText(requireContext(), uiIndicator.data.get(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setUpFocusBehavior() {
        text_distance_value.onFocusChangeListener = this
        text_distance_rate.onFocusChangeListener = this
        text_distance_location.onFocusChangeListener = this
        text_distance_date.onFocusChangeListener = this
        spinner_currency.onFocusChangeListener = this
        text_distance_comment.onFocusChangeListener = this
        distance_input_payment_method.onFocusChangeListener = this

        // And ensure that we do not show the keyboard when clicking these views
        val hideSoftKeyboardOnTouchListener = SoftKeyboardManager.HideSoftKeyboardOnTouchListener()
        spinner_currency.setOnTouchListener(hideSoftKeyboardOnTouchListener)
        distance_input_payment_method.setOnTouchListener(hideSoftKeyboardOnTouchListener)

        text_distance_date.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            setDateFormatter(dateFormatter)
            setOnTouchListener(hideSoftKeyboardOnTouchListener)
        }

        // Focused View
        if (focusedView == null) {
            focusedView = text_distance_value
        }

    }


    private fun constructDistance(): Distance {
        val distanceBuilder: DistanceBuilderFactory = when (editableItem) {
            null -> DistanceBuilderFactory()
                .setDistance(ModelUtils.tryParse(text_distance_value.text.toString(), BigDecimal.ZERO))
                .setRate(ModelUtils.tryParse(text_distance_rate.text.toString(), BigDecimal.ZERO))
            else -> DistanceBuilderFactory(editableItem!!)
                .setDistance(ModelUtils.tryParse(text_distance_value.text.toString(), editableItem!!.distance))
                .setRate(ModelUtils.tryParse(text_distance_rate.text.toString(), editableItem!!.rate))
        }

        val paymentMethod: PaymentMethod? =
            if (presenter.isUsePaymentMethods()) {
                distance_input_payment_method.selectedItem as PaymentMethod
            } else {
                null
            }

        return distanceBuilder
            .setTrip(parentTrip)
            .setLocation(text_distance_location.text.toString())
            .setDate(text_distance_date.date)
            .setTimezone(text_distance_date.timeZone)
            .setCurrency(spinner_currency.selectedItem.toString())
            .setComment(text_distance_comment.text.toString())
            .setPaymentMethod(paymentMethod)
            .build()
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(activity)
            .setTitle(getString(R.string.delete_item, editableItem!!.location))
            .setMessage(R.string.delete_sync_information)
            .setCancelable(true)
            .setPositiveButton(R.string.delete) { _, _ -> _deleteDistanceClicks.onNext(editableItem!!) }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .show()
    }

    override fun togglePaymentMethodFieldVisibility(): Consumer<in Boolean> {
        return Consumer { isVisible ->
            if (isVisible) {
                ViewCollections.run(paymentMethodsViewsList, ButterKnifeActions.setVisibility(View.VISIBLE))
            } else {
                ViewCollections.run(paymentMethodsViewsList, ButterKnifeActions.setVisibility(View.GONE))
            }
        }
    }

    override fun displayPaymentMethods(list: List<PaymentMethod>) {
        if (isAdded) {
            paymentMethodsAdapter.update(list)
            distance_input_payment_method.adapter = paymentMethodsAdapter
            if (editableItem != null) {
                // Here we manually loop through all payment methods and check for id == id in case the user changed this via "Manage"
                val distancePaymentMethod = editableItem!!.paymentMethod
                for (i in 0 until paymentMethodsAdapter.count) {
                    val paymentMethod = paymentMethodsAdapter.getItem(i)
                    if (paymentMethod != null && paymentMethod.id == distancePaymentMethod.id) {
                        distance_input_payment_method.setSelection(i)
                        break
                    }
                }
            }
        }
    }

    override fun getTextChangeStream(field: AutoCompleteField): Observable<CharSequence> {
        return when (field) {
            DistanceAutoCompleteField.Location -> text_distance_location.textChanges()
            DistanceAutoCompleteField.Comment -> text_distance_comment.textChanges()
            else -> throw IllegalArgumentException("Unsupported field type: $field")
        }
    }

    override fun displayAutoCompleteResults(field: AutoCompleteField, results: List<AutoCompleteResult<Distance>>) {
        if (isAdded) {
            val resultsAdapter = AutoCompleteArrayAdapter(requireContext(), results)
            when (field) {
                DistanceAutoCompleteField.Location -> {
                    text_distance_location.setAdapter(resultsAdapter)
                    text_distance_location.showDropDown()
                }
                DistanceAutoCompleteField.Comment -> {
                    text_distance_comment.setAdapter(resultsAdapter)
                    text_distance_comment.showDropDown()
                }
                else -> throw IllegalArgumentException("Unsupported field type: $field")
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = DistanceCreateEditFragment()

        const val ARG_SUGGESTED_DATE = "arg_suggested_date"

    }

}
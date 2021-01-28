package co.smartreceipts.android.distance.editor

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
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
import co.smartreceipts.android.databinding.UpdateDistanceBinding
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.distance.editor.currency.DistanceCurrencyCodeSupplier
import co.smartreceipts.android.fragments.WBFragment
import co.smartreceipts.android.model.AutoCompleteUpdateEvent
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.PaymentMethod
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.model.factory.DistanceBuilderFactory
import co.smartreceipts.android.model.utils.ModelUtils
import co.smartreceipts.android.persistence.DatabaseHelper
import co.smartreceipts.android.receipts.editor.paymentmethods.PaymentMethodsPresenter
import co.smartreceipts.android.receipts.editor.paymentmethods.PaymentMethodsView
import co.smartreceipts.android.utils.SoftKeyboardManager
import co.smartreceipts.android.widget.model.UiIndicator
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.widget.textChanges
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.sql.Date
import java.util.*
import javax.inject.Inject

class DistanceCreateEditFragment : WBFragment(), DistanceCreateEditView, View.OnFocusChangeListener,
    PaymentMethodsView {

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

    private lateinit var paymentMethodsViewsList: List<@JvmSuppressWildcards View>

    override val editableItem: Distance?
        get() = arguments?.getParcelable(Distance.PARCEL_KEY)

    private val parentTrip: Trip
        get() = arguments?.getParcelable(Trip.PARCEL_KEY) ?: throw IllegalStateException("Distance can't exist without parent trip")

    private var suggestedDate: Date = Date(Calendar.getInstance().timeInMillis)

    private lateinit var currencyListEditorPresenter: CurrencyListEditorPresenter

    private lateinit var resultsAdapter: AutoCompleteArrayAdapter<Distance>

    private var shouldHideResults: Boolean = false

    private var focusedView: View? = null

    private lateinit var snackbar: Snackbar

    private lateinit var paymentMethodsAdapter: FooterButtonArrayAdapter<PaymentMethod>

    private var itemToRemoveOrReAdd: AutoCompleteResult<Distance>? = null

    private var _binding: UpdateDistanceBinding? = null
    private val binding get() = _binding!!

    override val createDistanceClicks: Observable<Distance>
        get() = _createDistanceClicks

    override val updateDistanceClicks: Observable<Distance>
        get() = _updateDistanceClicks

    override val deleteDistanceClicks: Observable<Distance>
        get() = _deleteDistanceClicks

    override val hideAutoCompleteVisibilityClick: Observable<AutoCompleteUpdateEvent<Distance>>
        get() =_hideAutoCompleteVisibilityClicks

    override val unHideAutoCompleteVisibilityClick: Observable<AutoCompleteUpdateEvent<Distance>>
        get() =_unHideAutoCompleteVisibilityClicks

    private val _createDistanceClicks: Subject<Distance> = PublishSubject.create<Distance>().toSerialized()
    private val _updateDistanceClicks: Subject<Distance> = PublishSubject.create<Distance>().toSerialized()
    private val _deleteDistanceClicks: Subject<Distance> = PublishSubject.create<Distance>().toSerialized()
    private val _hideAutoCompleteVisibilityClicks: Subject<AutoCompleteUpdateEvent<Distance>> =
        PublishSubject.create<AutoCompleteUpdateEvent<Distance>>().toSerialized()
    private val _unHideAutoCompleteVisibilityClicks: Subject<AutoCompleteUpdateEvent<Distance>> =
        PublishSubject.create<AutoCompleteUpdateEvent<Distance>>().toSerialized()

    override fun onAttach(context: Context) {
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
                DefaultCurrencyListEditorView(requireContext()) { binding.spinnerCurrency },
                database,
                DistanceCurrencyCodeSupplier(parentTrip, editableItem),
                savedInstanceState
            )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = UpdateDistanceBinding.inflate(inflater, container, false)

        paymentMethodsViewsList = listOf(binding.distanceInputGuideImagePaymentMethod, binding.distanceInputPaymentMethod)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFocusBehavior()

        // Toolbar stuff
        when {
            navigationHandler.isDualPane -> binding.toolbar.toolbar.visibility = View.GONE
            else -> setSupportActionBar(binding.toolbar.toolbar)
        }

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_clear_24dp)
            setTitle(if (editableItem == null) R.string.dialog_mileage_title_create else R.string.dialog_mileage_title_update)
            subtitle = ""
        }

        binding.apply {
            if (editableItem == null) {
                // New Distance
                textDistanceDate.date = suggestedDate
                textDistanceRate.setText(presenter.getDefaultDistanceRate())
            } else {
                // Update distance
                textDistanceValue.setText(editableItem!!.decimalFormattedDistance)
                textDistanceRate.setText(editableItem!!.decimalFormattedRate)
                textDistanceLocation.setText(editableItem!!.location)
                textDistanceComment.setText(editableItem!!.comment)
                textDistanceDate.date = editableItem!!.date
                textDistanceDate.timeZone = editableItem!!.timeZone
            }
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
        if (::snackbar.isInitialized && snackbar.isShown) {
            snackbar.dismiss()
        }
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currencyListEditorPresenter.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(if (editableItem == null) R.menu.menu_save else R.menu.menu_save_delete, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        binding.textDistanceValue.onFocusChangeListener = this
        binding.textDistanceRate.onFocusChangeListener = this
        binding.textDistanceLocation.onFocusChangeListener = this
        binding.textDistanceDate.onFocusChangeListener = this
        binding.spinnerCurrency.onFocusChangeListener = this
        binding.textDistanceComment.onFocusChangeListener = this
        binding.distanceInputPaymentMethod.onFocusChangeListener = this

        // And ensure that we do not show the keyboard when clicking these views
        val hideSoftKeyboardOnTouchListener = SoftKeyboardManager.HideSoftKeyboardOnTouchListener()
        binding.spinnerCurrency.setOnTouchListener(hideSoftKeyboardOnTouchListener)
        binding.distanceInputPaymentMethod.setOnTouchListener(hideSoftKeyboardOnTouchListener)

        binding.textDistanceDate.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            setDateFormatter(dateFormatter)
            setOnTouchListener(hideSoftKeyboardOnTouchListener)
        }

        // Focused View
        if (focusedView == null) {
            focusedView = binding.textDistanceValue
        }

    }


    private fun constructDistance(): Distance {
        val distanceBuilder: DistanceBuilderFactory = when (editableItem) {
            null -> DistanceBuilderFactory()
                .setDistance(ModelUtils.tryParse(binding.textDistanceValue.text.toString()))
                .setRate(ModelUtils.tryParse(binding.textDistanceRate.text.toString()))
            else -> DistanceBuilderFactory(editableItem!!)
                .setDistance(ModelUtils.tryParse(binding.textDistanceValue.text.toString(), editableItem!!.distance))
                .setRate(ModelUtils.tryParse(binding.textDistanceRate.text.toString(), editableItem!!.rate))
        }

        val paymentMethod: PaymentMethod? =
            if (presenter.isUsePaymentMethods()) {
                binding.distanceInputPaymentMethod.selectedItem as PaymentMethod
            } else {
                null
            }

        return distanceBuilder
            .setTrip(parentTrip)
            .setLocation(binding.textDistanceLocation.text.toString())
            .setDate(binding.textDistanceDate.date)
            .setTimezone(binding.textDistanceDate.timeZone)
            .setCurrency(binding.spinnerCurrency.selectedItem.toString())
            .setComment(binding.textDistanceComment.text.toString())
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
            run {
                for (v in paymentMethodsViewsList) {
                    v.visibility = if (isVisible) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun displayPaymentMethods(list: List<PaymentMethod>) {
        if (isAdded) {
            paymentMethodsAdapter.update(list)
            binding.distanceInputPaymentMethod.adapter = paymentMethodsAdapter
            if (editableItem != null) {
                // Here we manually loop through all payment methods and check for id == id in case the user changed this via "Manage"
                val distancePaymentMethod = editableItem!!.paymentMethod
                for (i in 0 until paymentMethodsAdapter.count) {
                    val paymentMethod = paymentMethodsAdapter.getItem(i)
                    if (paymentMethod != null && paymentMethod.id == distancePaymentMethod.id) {
                        binding.distanceInputPaymentMethod.setSelection(i)
                        break
                    }
                }
            }
        }
    }

    override fun getTextChangeStream(field: AutoCompleteField): Observable<CharSequence> {
        return when (field) {
            DistanceAutoCompleteField.Location -> binding.textDistanceLocation.textChanges()
            DistanceAutoCompleteField.Comment -> binding.textDistanceComment.textChanges()
            else -> throw IllegalArgumentException("Unsupported field type: $field")
        }
    }

    override fun displayAutoCompleteResults(field: AutoCompleteField, results: MutableList<AutoCompleteResult<Distance>>) {
        if (isAdded) {
            if (!shouldHideResults) {
                if (::snackbar.isInitialized && snackbar.isShown) {
                    snackbar.dismiss()
                }
                resultsAdapter = AutoCompleteArrayAdapter(requireContext(), results, this)
                when (field) {
                    DistanceAutoCompleteField.Location -> {
                        binding.textDistanceLocation.apply {
                            setAdapter(resultsAdapter)
                            if (hasFocus()) {
                                showDropDown()
                            }
                        }
                    }
                    DistanceAutoCompleteField.Comment -> {
                        binding.textDistanceComment.apply {
                            setAdapter(resultsAdapter)
                            if (hasFocus()) {
                                showDropDown()
                            }
                        }
                    }
                    else -> throw IllegalArgumentException("Unsupported field type: $field")
                }
            } else {
                shouldHideResults = false
            }
        }
    }

    override fun fillValueField(autoCompleteResult: AutoCompleteResult<Distance>) {
        shouldHideResults = true
        if (binding.textDistanceLocation.isPopupShowing) {
            binding.textDistanceLocation.apply {
                setText(autoCompleteResult.displayName)
                setSelection(text.length)
                dismissDropDown()
            }
        } else {
            binding.textDistanceComment.apply {
                setText(autoCompleteResult.displayName)
                setSelection(text.length)
                dismissDropDown()
            }
        }
        SoftKeyboardManager.hideKeyboard(focusedView)
    }

    override fun sendAutoCompleteHideEvent(autoCompleteResult: AutoCompleteResult<Distance>) {
        SoftKeyboardManager.hideKeyboard(focusedView)
        itemToRemoveOrReAdd = autoCompleteResult
        when(binding.textDistanceLocation.isPopupShowing) {
            true -> _hideAutoCompleteVisibilityClicks.onNext(
                        AutoCompleteUpdateEvent(autoCompleteResult, DistanceAutoCompleteField.Location, resultsAdapter.getPosition(autoCompleteResult)))
            false -> _hideAutoCompleteVisibilityClicks.onNext(
                        AutoCompleteUpdateEvent(autoCompleteResult, DistanceAutoCompleteField.Comment, resultsAdapter.getPosition(autoCompleteResult)))
        }
    }

    override fun removeValueFromAutoComplete(position: Int) {
        activity!!.runOnUiThread {
            if (position in 0 until resultsAdapter.count) {
                itemToRemoveOrReAdd = resultsAdapter.getItem(position)
                resultsAdapter.remove(itemToRemoveOrReAdd)
                resultsAdapter.notifyDataSetChanged()
                val view = activity!!.findViewById<ConstraintLayout>(R.id.update_distance_layout)
                snackbar = Snackbar.make(view, getString(
                        R.string.item_removed_from_auto_complete, itemToRemoveOrReAdd!!.displayName), Snackbar.LENGTH_LONG)
                snackbar.setAction(R.string.undo) {
                    if (binding.textDistanceLocation.hasFocus()) {
                        _unHideAutoCompleteVisibilityClicks.onNext(
                            AutoCompleteUpdateEvent(itemToRemoveOrReAdd, DistanceAutoCompleteField.Location, position)
                        )
                    } else {
                        _unHideAutoCompleteVisibilityClicks.onNext(
                            AutoCompleteUpdateEvent(itemToRemoveOrReAdd, DistanceAutoCompleteField.Comment, position)
                        )
                    }
                }
                snackbar.show()
            }
        }
    }

    override fun sendAutoCompleteUnHideEvent(position: Int) {
        activity!!.runOnUiThread {
            resultsAdapter.insert(itemToRemoveOrReAdd, position)
            resultsAdapter.notifyDataSetChanged()
            Toast.makeText(context, R.string.result_restored, Toast.LENGTH_LONG).show()
        }
    }

    override fun displayAutoCompleteError() {
        activity!!.runOnUiThread {
            Toast.makeText(activity, R.string.result_restore_failed, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = DistanceCreateEditFragment()

        const val ARG_SUGGESTED_DATE = "arg_suggested_date"

    }

}
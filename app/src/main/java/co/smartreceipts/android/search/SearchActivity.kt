package co.smartreceipts.android.search

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import co.smartreceipts.android.R
import co.smartreceipts.android.databinding.ActivitySearchBinding
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.search.delegates.HeaderItem
import co.smartreceipts.android.sync.BackupProvidersManager
import com.jakewharton.rxbinding3.appcompat.queryTextChanges
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_search.*
import javax.inject.Inject

class SearchActivity : AppCompatActivity(), SearchView {

    companion object {
        const val RESULT_RECEIPT = 122
        const val RESULT_TRIP = 123

        const val EXTRA_RESULT = "extra_result"
    }

    override val inputChanges
        get() = search_view.queryTextChanges().skipInitialValue()

    @Inject
    lateinit var presenter: SearchPresenter

    @Inject
    lateinit var dateFormatter: DateFormatter

    @Inject
    lateinit var backupProvidersManager: BackupProvidersManager

    private lateinit var adapter: SearchResultsDelegationAdapter

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // sets the focus and opens the keyboard
        search_view.isIconified = false

        adapter = SearchResultsDelegationAdapter({ navigateToTrip(it) }, { navigateToReceipt(it) }, dateFormatter, backupProvidersManager.syncProvider)

        results_list.layoutManager = LinearLayoutManager(this)
        results_list.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onStart() {
        super.onStart()

        presenter.subscribe()
    }

    override fun onStop() {
        presenter.unsubscribe()

        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun presentSearchResults(searchResults: SearchInteractor.SearchResults) {
        TransitionManager.beginDelayedTransition(container, AutoTransition().setDuration(100))

        if (searchResults.isEmpty()) {
            results_list.visibility = View.GONE

            search_hint.visibility = if (search_view.query.isEmpty()) View.VISIBLE else View.GONE
            no_results_text.visibility = if (search_view.query.isNotEmpty()) View.VISIBLE else View.GONE

        } else {
            results_list.visibility = View.VISIBLE
            search_hint.visibility = View.GONE
            no_results_text.visibility = View.GONE
        }

        val results : MutableList<Any> = arrayListOf()
        if (searchResults.trips.isNotEmpty()) {
            results.add(HeaderItem(getString(R.string.reports_title)))
            results.addAll(searchResults.trips)
        }
        if (searchResults.receipts.isNotEmpty()) {
            results.add(HeaderItem(getString(R.string.report_info_receipts)))
            results.addAll(searchResults.receipts)
        }
        adapter.items = results
        adapter.notifyDataSetChanged()
    }

    private fun navigateToTrip(trip: Trip) {
        setResult(RESULT_TRIP, Intent().putExtra(EXTRA_RESULT, trip))
        finish()
    }

    private fun navigateToReceipt(receipt: Receipt) {
        setResult(RESULT_RECEIPT, Intent().putExtra(EXTRA_RESULT, receipt))
        finish()
    }
}
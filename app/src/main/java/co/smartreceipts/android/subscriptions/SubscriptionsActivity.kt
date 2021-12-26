package co.smartreceipts.android.subscriptions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import co.smartreceipts.android.R
import co.smartreceipts.android.databinding.ActivitySubscriptionsBinding
import com.jakewharton.rxbinding3.view.clicks
import dagger.android.AndroidInjection
import io.reactivex.Observable
import javax.inject.Inject

class SubscriptionsActivity : AppCompatActivity(), SubscriptionsView {

    // TODO: 21.12.2021 how to handle users with bought ocr scans?
    // TODO: 21.12.2021 implement A/B
    // TODO: 21.12.2021 implement feature on/off
    // TODO: 20.12.2021 add translations
    // TODO: 20.12.2021 we need user to be logged in to purchase new plan. ?
    // TODO: 21.12.2021 the behaviour of SR Plus app?

    @Inject
    lateinit var presenter: SubscriptionsPresenter

    override val standardSubscriptionClicks: Observable<Unit>
        get() = binding.cardStandard.clicks().filter { !binding.yourPlanStandard.isVisible }
    override val premiumSubscriptionClicks: Observable<Unit>
        get() = binding.cardPremium.clicks().filter { !binding.yourPlanPremium.isVisible }
    override val cancelSubscriptionInfoClicks: Observable<Unit> get() = binding.cancelSubscriptionInfo.clicks()

    private var _binding: ActivitySubscriptionsBinding? = null
    private val binding get() = _binding!!
m
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        _binding = ActivitySubscriptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardStandard.isVisible = false
        binding.cardPremium.isVisible = false

        binding.success.buttonContinue.setOnClickListener { finish() }
        binding.success.buttonClose.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()

        setSupportActionBar(binding.toolbar.toolbar)

        supportActionBar?.apply {
            setTitle(R.string.menu_main_subscriptions)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        presenter.subscribe()
    }

    override fun onStop() {
        presenter.unsubscribe()
        super.onStop()
    }

    override fun presentStandardPlan(price: String?) {

        binding.cardStandard.isVisible = true

        if (price == null) { // this plan is current
            binding.priceStandardContainer.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.subscription_current_plan_bg)

            binding.priceStandard.isVisible = false
            binding.perMonthStandard.isVisible = false
            binding.yourPlanStandard.isVisible = true
        } else {
            binding.priceStandardContainer.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.violet)

            binding.priceStandard.text = price

            binding.priceStandard.isVisible = true
            binding.perMonthStandard.isVisible = true
            binding.yourPlanStandard.isVisible = false
        }
    }

    override fun presentPremiumPlan(price: String?) {

        binding.cardPremium.isVisible = true

        if (price == null) { // this plan is current
            binding.pricePremiumContainer.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.subscription_current_plan_bg)

            binding.pricePremium.isVisible = false
            binding.perMonthPremium.isVisible = false
            binding.yourPlanPremium.isVisible = true
        } else {
            binding.pricePremiumContainer.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.violet)

            binding.pricePremium.text = price

            binding.pricePremium.isVisible = true
            binding.perMonthPremium.isVisible = true
            binding.yourPlanPremium.isVisible = false
        }
    }

    override fun redirectToPlayStoreSubscriptions() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/account/subscriptions")
            )
        )
    }

    override fun presentCancelInfo(isVisible: Boolean) {
        binding.cancelSubscriptionInfo.isVisible = isVisible
    }

    override fun presentSuccessSubscription() {
        supportActionBar?.hide()
        binding.success.root.isVisible = true
    }

    override fun presentFailedSubscription() {
        Toast.makeText(this, R.string.purchase_failed, Toast.LENGTH_LONG).show()
    }
}
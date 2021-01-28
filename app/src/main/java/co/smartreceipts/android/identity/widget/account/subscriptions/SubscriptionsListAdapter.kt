package co.smartreceipts.android.identity.widget.account.subscriptions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.smartreceipts.android.R
import co.smartreceipts.android.databinding.ItemSubscriptionBinding
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.purchases.subscriptions.RemoteSubscription
import java.util.*

class SubscriptionsListAdapter(private val dateFormatter: DateFormatter) :
    RecyclerView.Adapter<SubscriptionsListAdapter.SubscriptionViewHolder>() {

    private var subscriptions = emptyList<RemoteSubscription>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder = SubscriptionViewHolder(ItemSubscriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = subscriptions.size

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        val item = subscriptions[position]

        holder.binding.subscriptionInfo.text = item.inAppPurchase.name

        val formattedDate = dateFormatter.getFormattedDate(item.expirationDate, TimeZone.getDefault())
        holder.binding.subscriptionExpiration.text = holder.itemView.context.getString(R.string.subscription_expiration, formattedDate)
    }

    fun setSubscriptions(subscriptions: List<RemoteSubscription>) {
        this.subscriptions = subscriptions
        notifyDataSetChanged()
    }

    class SubscriptionViewHolder internal constructor(val binding: ItemSubscriptionBinding) : RecyclerView.ViewHolder(binding.root)
}
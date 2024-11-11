package com.wops.receiptsgo.identity.widget.account.subscriptions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wops.receiptsgo.R
import com.wops.receiptsgo.databinding.ItemSubscriptionBinding
import com.wops.receiptsgo.date.DateFormatter
import com.wops.receiptsgo.purchases.subscriptions.RemoteSubscription
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
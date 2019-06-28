package co.smartreceipts.android.identity.widget.account.subscriptions

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.smartreceipts.android.R
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.purchases.subscriptions.RemoteSubscription
import kotlinx.android.synthetic.main.item_subscription.view.*
import java.util.*

class SubscriptionsListAdapter(private val dateFormatter: DateFormatter) :
    RecyclerView.Adapter<SubscriptionsListAdapter.SubscriptionViewHolder>() {

    private var subscriptions = emptyList<RemoteSubscription>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.item_subscription, parent, false)

        return SubscriptionViewHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return subscriptions.size
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        val item = subscriptions[position]

        holder.subscriptionName.text = item.inAppPurchase.name

        val formattedDate = dateFormatter.getFormattedDate(item.expirationDate, TimeZone.getDefault())
        holder.subscriptionExpirationDate.text = holder.itemView.context.getString(R.string.subscription_expiration, formattedDate)
    }

    fun setSubscriptions(subscriptions: List<RemoteSubscription>) {
        this.subscriptions = subscriptions
        notifyDataSetChanged()
    }

    class SubscriptionViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val subscriptionName: TextView = itemView.subscription_info
        internal val subscriptionExpirationDate: TextView = itemView.subscription_expiration

    }
}
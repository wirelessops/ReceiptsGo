package co.smartreceipts.android.ocr.widget.configuration

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.smartreceipts.android.R
import co.smartreceipts.android.purchases.model.AvailablePurchase
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.ocr_purchase_list_item.view.*
import java.util.*


class OcrPurchasesListAdapter(private val headerView: View) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val availablePurchaseClickSubject = PublishSubject.create<AvailablePurchase>()
    private var availablePurchases = emptyList<AvailablePurchase>()

    val availablePurchaseClicks: Observable<AvailablePurchase>
        get() = availablePurchaseClickSubject


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(headerView)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.ocr_purchase_list_item, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val availablePurchase = availablePurchases[position - 1]

            holder.apply {
                purchaseName.text = availablePurchase.title
                purchaseDescription.text = availablePurchase.description
                purchasePrice.text = availablePurchase.price
                parentView.setOnClickListener { ignored -> availablePurchaseClickSubject.onNext(availablePurchase) }
            }

        }
    }

    override fun getItemCount(): Int {
        return availablePurchases.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_HEADER
        } else {
            TYPE_ITEM
        }
    }

    fun setAvailablePurchases(availablePurchases: List<AvailablePurchase>) {
        this.availablePurchases = ArrayList(availablePurchases)
        notifyDataSetChanged()
    }

    private class HeaderViewHolder internal constructor(internal val headerView: View) : RecyclerView.ViewHolder(headerView)

    internal class ItemViewHolder(var parentView: View) : RecyclerView.ViewHolder(parentView) {
        var purchaseName: TextView = parentView.purchase_name
        var purchaseDescription: TextView = parentView.purchase_description
        var purchasePrice: TextView = parentView.purchase_price
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }
}

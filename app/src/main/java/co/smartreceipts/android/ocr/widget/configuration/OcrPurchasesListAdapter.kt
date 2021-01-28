package co.smartreceipts.android.ocr.widget.configuration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.smartreceipts.android.R
import co.smartreceipts.android.purchases.model.AvailablePurchase
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_ocr_purchase.view.*


class OcrPurchasesListAdapter : RecyclerView.Adapter<OcrPurchasesListAdapter.PurchaseViewHolder>() {

    private val availablePurchaseClickSubject = PublishSubject.create<AvailablePurchase>()
    private var availablePurchases = emptyList<AvailablePurchase>()

    val availablePurchaseClicks: Observable<AvailablePurchase>
        get() = availablePurchaseClickSubject


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ocr_purchase, parent, false)
        return PurchaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        val availablePurchase = availablePurchases[position]

        holder.apply {
            purchaseName.text = availablePurchase.title
            purchaseDescription.text = availablePurchase.description
            purchasePrice.text = availablePurchase.price
            itemView.setOnClickListener { availablePurchaseClickSubject.onNext(availablePurchase) }
        }
    }
    override fun getItemCount(): Int {
        return availablePurchases.size
    }

    fun setAvailablePurchases(availablePurchases: List<AvailablePurchase>) {
        this.availablePurchases = availablePurchases
        notifyDataSetChanged()
    }

    class PurchaseViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val purchaseName: TextView = itemView.purchase_name
        val purchaseDescription: TextView = itemView.purchase_description
        val purchasePrice: TextView = itemView.purchase_price
    }
}

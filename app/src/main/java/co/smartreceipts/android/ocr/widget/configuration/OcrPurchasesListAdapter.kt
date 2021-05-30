package co.smartreceipts.android.ocr.widget.configuration

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.smartreceipts.android.databinding.ItemOcrPurchaseBinding
import co.smartreceipts.android.purchases.model.AvailablePurchase
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


class OcrPurchasesListAdapter : RecyclerView.Adapter<OcrPurchasesListAdapter.PurchaseViewHolder>() {

    private val availablePurchaseClickSubject = PublishSubject.create<AvailablePurchase>()
    private var availablePurchases = emptyList<AvailablePurchase>()

    val availablePurchaseClicks: Observable<AvailablePurchase>
        get() = availablePurchaseClickSubject


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder =
        PurchaseViewHolder(ItemOcrPurchaseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        val availablePurchase = availablePurchases[position]

        holder.binding.apply {
            purchaseName.text = availablePurchase.title
            purchaseDescription.text = availablePurchase.description
            purchasePrice.text = availablePurchase.price
        }

        holder.itemView.setOnClickListener { availablePurchaseClickSubject.onNext(availablePurchase) }
    }

    override fun getItemCount(): Int = availablePurchases.size

    fun setAvailablePurchases(availablePurchases: List<AvailablePurchase>) {
        this.availablePurchases = availablePurchases
        notifyDataSetChanged()
    }

    class PurchaseViewHolder internal constructor(val binding: ItemOcrPurchaseBinding) : RecyclerView.ViewHolder(binding.root)
}

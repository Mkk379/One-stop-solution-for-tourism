package com.tourism.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tourism.app.databinding.ItemBookingBinding
import com.tourism.app.databinding.ItemListingBinding

class ListingAdapter(private val onAction: (Listing) -> Unit) : RecyclerView.Adapter<ListingAdapter.VH>() {
    private var items: List<Listing> = emptyList()
    private var actionText = "Book"

    class VH(val b: ItemListingBinding) : RecyclerView.ViewHolder(b.root)

    fun submit(list: List<Listing>, action: String) {
        items = list
        actionText = action
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemListingBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val l = items[position]
        holder.b.tvName.text = l.name
        holder.b.tvMeta.text = typeLabel(l.type) + "  •  " + l.location
        holder.b.tvPrice.text = money(l.price)
        holder.b.tvDesc.text = l.description
        holder.b.tvDesc.visibility = if (l.description.isEmpty()) View.GONE else View.VISIBLE
        holder.b.btnAction.text = actionText
        holder.b.btnAction.setOnClickListener { onAction(l) }
    }
}

class BookingAdapter(
    private val canConfirm: Boolean,
    private val onConfirm: (Booking) -> Unit,
    private val onCancel: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.VH>() {
    private var items: List<Booking> = emptyList()

    class VH(val b: ItemBookingBinding) : RecyclerView.ViewHolder(b.root)

    fun submit(list: List<Booking>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val bk = items[position]
        holder.b.tvTitle.text = bk.listingName + " (" + typeLabel(bk.listingType) + ")"
        holder.b.tvDetails.text = "Booked by: " + bk.userName + "\nDate: " + bk.date +
            "\nQuantity: " + bk.quantity + "\nTotal: " + money(bk.total)
        holder.b.tvStatus.text = "Status: " + bk.status
        val pending = bk.status == BookingStatus.PENDING
        holder.b.rowButtons.visibility = if (pending) View.VISIBLE else View.GONE
        holder.b.btnConfirm.visibility = if (canConfirm) View.VISIBLE else View.GONE
        holder.b.btnConfirm.setOnClickListener { onConfirm(bk) }
        holder.b.btnCancel.setOnClickListener { onCancel(bk) }
    }
}

package com.marwadiuniversity.flourmate.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.marwadiuniversity.flourmate.R
import com.marwadiuniversity.flourmate.models.Mill
import com.marwadiuniversity.flourmate.utils.ImageLoader

class MillAdapter(
    private var mills: List<Mill>,
    private val onMillClick: (Mill) -> Unit
) : RecyclerView.Adapter<MillAdapter.MillViewHolder>() {

    class MillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivMillGraphic: ImageView = itemView.findViewById(R.id.ivMillGraphic)
        val tvMillName: TextView = itemView.findViewById(R.id.tvMillName)
        val tvMillAddress: TextView = itemView.findViewById(R.id.tvMillAddress)
        val tvMillRating: TextView = itemView.findViewById(R.id.tvMillRating)
        val tvStatusBadge: TextView = itemView.findViewById(R.id.tvStatusBadge)
        val tvPriceRange: TextView = itemView.findViewById(R.id.tvPriceRange)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MillViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mill, parent, false)
        return MillViewHolder(view)
    }

    override fun onBindViewHolder(holder: MillViewHolder, position: Int) {
        val mill = mills[position]
        holder.tvMillName.text = mill.name
        holder.tvMillAddress.text = mill.address
        holder.tvMillRating.text = "⭐ ${mill.rating}"
        holder.tvStatusBadge.text = if (mill.isAvailable) "Open" else "Closed"
        holder.tvPriceRange.text = "From ₹${mill.minPricePerKg.toInt()}/kg"

        ImageLoader.loadImage(holder.ivMillGraphic, mill.imageUri, R.drawable.ic_flour_grain)

        holder.itemView.setOnClickListener {
            onMillClick(mill)
        }
    }

    override fun getItemCount(): Int = mills.size

    fun updateMills(newMills: List<Mill>) {
        mills = newMills
        notifyDataSetChanged()
    }
}
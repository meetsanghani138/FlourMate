package com.marwadiuniversity.flourmate.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.marwadiuniversity.flourmate.R
import com.marwadiuniversity.flourmate.models.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private var orders: List<Order>,
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvMillName: TextView = itemView.findViewById(R.id.tvMillName)
        val tvOrderDetails: TextView = itemView.findViewById(R.id.tvOrderDetails)
        val tvOrderAmount: TextView = itemView.findViewById(R.id.tvOrderAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val typeTag = if (order.orderType == "PICKUP") "🏪 Pickup" else "🚚 Delivery"
        holder.tvOrderId.text = "Order #${order.orderNumber} • $typeTag"
        holder.tvOrderStatus.text = order.status
        holder.tvMillName.text = order.millName

        when (order.status) {
            "Delivered", "Completed" -> {
                holder.tvOrderStatus.setTextColor(holder.itemView.context.getColor(R.color.primary_green))
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_badge_green)
            }
            "Cancelled", "Rejected" -> {
                holder.tvOrderStatus.setTextColor(holder.itemView.context.getColor(R.color.error))
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_badge_green)
            }
            "Ready for Pickup", "Pending" -> {
                holder.tvOrderStatus.setTextColor(holder.itemView.context.getColor(R.color.main_text))
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_badge_gold)
            }
            else -> {
                holder.tvOrderStatus.setTextColor(holder.itemView.context.getColor(R.color.primary_green))
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_badge_green)
            }
        }

        val dateFormat = SimpleDateFormat("dd MMM, yyyy • hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(order.timestamp))
        holder.tvOrderDetails.text = "${order.itemCount} Items • $dateStr"
        holder.tvOrderAmount.text = "₹${"%.2f".format(order.totalAmount)}"

        holder.itemView.setOnClickListener {
            onOrderClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
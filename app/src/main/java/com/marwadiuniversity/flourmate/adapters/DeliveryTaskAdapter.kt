package com.marwadiuniversity.flourmate.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.marwadiuniversity.flourmate.R
import com.marwadiuniversity.flourmate.models.Order

class DeliveryTaskAdapter(
    private var tasks: List<Order>,
    private val onTaskClick: (Order) -> Unit
) : RecyclerView.Adapter<DeliveryTaskAdapter.DeliveryViewHolder>() {

    class DeliveryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTaskId: TextView = itemView.findViewById(R.id.tvTaskId)
        val tvTaskStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvPickupAddress: TextView = itemView.findViewById(R.id.tvPickupAddress)
        val tvDeliveryAddress: TextView = itemView.findViewById(R.id.tvDeliveryAddress)
        val tvEarningFee: TextView = itemView.findViewById(R.id.tvEarningFee)
        val btnTaskAction: MaterialButton = itemView.findViewById(R.id.btnTaskAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_delivery_task, parent, false)
        return DeliveryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        val task = tasks[position]
        holder.tvTaskId.text = "Order #${task.orderNumber}"
        holder.tvTaskStatus.text = task.status
        holder.tvCustomerName.text = "Customer: ${task.customerName}"
        holder.tvPickupAddress.text = "Pickup: ${task.pickupAddress}"
        holder.tvDeliveryAddress.text = "Drop: ${task.deliveryAddress}"
        holder.tvEarningFee.text = "Earning: ₹45.00"

        holder.btnTaskAction.setOnClickListener {
            onTaskClick(task)
        }
        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Order>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}

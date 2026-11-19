package com.marwadiuniversity.flourmate.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.marwadiuniversity.flourmate.R
import com.marwadiuniversity.flourmate.models.CartItem
import com.marwadiuniversity.flourmate.utils.ImageLoader

class CartAdapter(
    private var cartItems: List<CartItem>,
    private val onQuantityChange: (CartItem, Int) -> Unit,
    private val onDeleteItem: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCartItemImage: ImageView = itemView.findViewById(R.id.ivCartItemImage)
        val tvCartItemName: TextView = itemView.findViewById(R.id.tvCartItemName)
        val tvCartItemPrice: TextView = itemView.findViewById(R.id.tvCartItemPrice)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val btnMinus: ImageView = itemView.findViewById(R.id.btnMinus)
        val btnPlus: ImageView = itemView.findViewById(R.id.btnPlus)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        holder.tvCartItemName.text = item.productName
        holder.tvCartItemPrice.text = "₹${"%.2f".format(item.price * item.quantity)}"
        holder.tvQuantity.text = item.quantity.toString()

        ImageLoader.loadImage(holder.ivCartItemImage, item.imageUri, R.drawable.ic_flour_grain)

        holder.btnMinus.setOnClickListener {
            onQuantityChange(item, item.quantity - 1)
        }

        holder.btnPlus.setOnClickListener {
            onQuantityChange(item, item.quantity + 1)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteItem(item)
        }
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateItems(newItems: List<CartItem>) {
        cartItems = newItems
        notifyDataSetChanged()
    }
}
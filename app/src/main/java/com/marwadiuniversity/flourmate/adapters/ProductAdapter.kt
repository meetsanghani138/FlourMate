package com.marwadiuniversity.flourmate.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.marwadiuniversity.flourmate.R
import com.marwadiuniversity.flourmate.models.Product
import com.marwadiuniversity.flourmate.utils.ImageLoader

class ProductAdapter(
    private var products: List<Product>,
    private val onAddToCartClick: (Product) -> Unit,
    private val onItemClick: ((Product) -> Unit)? = null,
    private val onDeleteClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductIcon: ImageView = itemView.findViewById(R.id.ivProductIcon)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductCategory: TextView = itemView.findViewById(R.id.tvProductCategory)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val btnAddToCart: MaterialButton = itemView.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.tvProductName.text = product.name
        holder.tvProductCategory.text = "${product.category} • ${product.unitVariant}"
        holder.tvProductPrice.text = "₹${"%.2f".format(product.price)}"

        ImageLoader.loadImage(holder.ivProductIcon, product.imageUri, R.drawable.ic_flour_grain)

        if (onDeleteClick != null) {
            holder.btnAddToCart.text = "Delete"
            holder.btnAddToCart.setBackgroundColor(holder.itemView.context.getColor(R.color.error))
            holder.btnAddToCart.setOnClickListener { onDeleteClick.invoke(product) }
        } else {
            holder.btnAddToCart.text = "Add"
            holder.btnAddToCart.setOnClickListener { onAddToCartClick(product) }
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(product)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
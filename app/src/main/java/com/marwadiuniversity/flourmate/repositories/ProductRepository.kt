package com.marwadiuniversity.flourmate.repositories

import com.marwadiuniversity.flourmate.database.CartDao
import com.marwadiuniversity.flourmate.database.ProductDao
import com.marwadiuniversity.flourmate.models.CartItem
import com.marwadiuniversity.flourmate.models.Product
import com.marwadiuniversity.flourmate.network.FirebaseSyncManager

class ProductRepository(
    private val productDao: ProductDao,
    private val cartDao: CartDao
) {

    suspend fun addProduct(product: Product): Long {
        val productId = productDao.insertProduct(product)
        val fullProduct = product.copy(id = productId)
        FirebaseSyncManager.syncPublishProduct(fullProduct)
        return productId
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
        FirebaseSyncManager.syncPublishProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
        FirebaseSyncManager.syncDeleteProduct(product.id)
    }

    suspend fun deleteProductById(productId: Long) {
        productDao.deleteProductById(productId)
        FirebaseSyncManager.syncDeleteProduct(productId)
    }

    suspend fun getProductById(productId: Long): Product? {
        return productDao.getProductById(productId)
    }

    suspend fun getProductsByMillId(millId: Long): List<Product> {
        return productDao.getProductsByMillId(millId)
    }

    suspend fun getAllProducts(): List<Product> {
        return productDao.getAllProducts()
    }

    suspend fun searchProducts(query: String): List<Product> {
        return productDao.searchProducts(query)
    }

    // Cart operations
    suspend fun addToCart(userId: Long, product: Product): Long {
        val existing = cartDao.getCartItemByProduct(userId, product.id)
        return if (existing != null) {
            val updated = existing.copy(quantity = existing.quantity + 1)
            cartDao.updateCartItem(updated)
            existing.id
        } else {
            val item = CartItem(
                userId = userId,
                productId = product.id,
                millId = product.millId,
                millName = product.millName,
                productName = product.name,
                price = product.price,
                quantity = 1,
                unitVariant = product.unitVariant,
                imageUri = product.imageUri
            )
            cartDao.insertCartItem(item)
        }
    }

    suspend fun updateCartQuantity(cartItemId: Long, quantity: Int, userId: Long) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(cartItemId)
        } else {
            val items = cartDao.getCartItemsByUserId(userId)
            val item = items.find { it.id == cartItemId }
            if (item != null) {
                cartDao.updateCartItem(item.copy(quantity = quantity))
            }
        }
    }

    suspend fun removeCartItem(cartItemId: Long) {
        cartDao.deleteCartItem(cartItemId)
    }

    suspend fun getCartItems(userId: Long): List<CartItem> {
        return cartDao.getCartItemsByUserId(userId)
    }

    suspend fun clearCart(userId: Long) {
        cartDao.clearCart(userId)
    }
}
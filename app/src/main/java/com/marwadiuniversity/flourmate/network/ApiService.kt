package com.marwadiuniversity.flourmate.network

import com.marwadiuniversity.flourmate.models.DeliveryLocation
import com.marwadiuniversity.flourmate.models.Order
import com.marwadiuniversity.flourmate.models.Product

interface ApiService {
    suspend fun syncProducts(millId: Long): List<Product>
    suspend fun uploadProduct(product: Product): Boolean
    suspend fun syncOrders(userId: Long, role: String): List<Order>
    suspend fun updateOrderStatus(orderId: Long, status: String): Boolean
    suspend fun pushDeliveryLocation(location: DeliveryLocation): Boolean
    suspend fun fetchLatestDeliveryLocation(orderId: Long): DeliveryLocation?
}

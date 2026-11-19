package com.marwadiuniversity.flourmate.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.marwadiuniversity.flourmate.models.Order

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)

    @Query("UPDATE orders SET deliveryPartnerId = :partnerId, status = :status WHERE id = :orderId")
    suspend fun assignDeliveryPartner(orderId: Long, partnerId: Long, status: String = "Assigned")

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: Long): Order?

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY timestamp DESC")
    suspend fun getOrdersByCustomerId(customerId: Long): List<Order>

    @Query("SELECT * FROM orders WHERE millId = :millId ORDER BY timestamp DESC")
    suspend fun getOrdersByMillId(millId: Long): List<Order>

    @Query("SELECT * FROM orders WHERE deliveryPartnerId = :partnerId ORDER BY timestamp DESC")
    suspend fun getOrdersByDeliveryPartnerId(partnerId: Long): List<Order>

    @Query("SELECT * FROM orders WHERE status IN ('Accepted', 'Preparing', 'Ready for Pickup', 'Assigned') AND (deliveryPartnerId = 0 OR deliveryPartnerId = :partnerId) ORDER BY timestamp DESC")
    suspend fun getAvailableDeliveryRequests(partnerId: Long): List<Order>

    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    suspend fun getAllOrders(): List<Order>
}

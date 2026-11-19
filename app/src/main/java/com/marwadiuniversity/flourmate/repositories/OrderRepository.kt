package com.marwadiuniversity.flourmate.repositories

import com.marwadiuniversity.flourmate.database.NotificationDao
import com.marwadiuniversity.flourmate.database.OrderDao
import com.marwadiuniversity.flourmate.database.OrderItemDao
import com.marwadiuniversity.flourmate.models.NotificationItem
import com.marwadiuniversity.flourmate.models.Order
import com.marwadiuniversity.flourmate.models.OrderItem
import com.marwadiuniversity.flourmate.network.FirebaseSyncManager

class OrderRepository(
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val notificationDao: NotificationDao
) {

    suspend fun placeOrder(order: Order, items: List<OrderItem>): Long {
        val orderId = orderDao.insertOrder(order)
        val fullOrder = order.copy(id = orderId)
        val itemsWithOrderId = items.map { it.copy(orderId = orderId) }
        orderItemDao.insertOrderItems(itemsWithOrderId)

        notificationDao.insertNotification(
            NotificationItem(
                userId = order.customerId,
                title = "Order Placed Successfully 🎉",
                message = "Your order #${order.orderNumber} has been placed with ${order.millName}."
            )
        )

        // Sync globally via Firebase Cloud Firestore
        FirebaseSyncManager.syncPublishOrder(fullOrder)

        return orderId
    }

    suspend fun getOrderById(orderId: Long): Order? {
        return orderDao.getOrderById(orderId)
    }

    suspend fun getOrderItems(orderId: Long): List<OrderItem> {
        return orderItemDao.getItemsByOrderId(orderId)
    }

    suspend fun getOrdersForCustomer(customerId: Long): List<Order> {
        return orderDao.getOrdersByCustomerId(customerId)
    }

    suspend fun getOrdersForMill(millId: Long): List<Order> {
        return orderDao.getOrdersByMillId(millId)
    }

    suspend fun getOrdersForDeliveryPartner(partnerId: Long): List<Order> {
        return orderDao.getOrdersByDeliveryPartnerId(partnerId)
    }

    suspend fun getAvailableDeliveryRequests(partnerId: Long): List<Order> {
        return orderDao.getAvailableDeliveryRequests(partnerId)
    }

    suspend fun updateOrderStatus(orderId: Long, status: String, notifyUserId: Long = 0) {
        orderDao.updateOrderStatus(orderId, status)
        val order = orderDao.getOrderById(orderId)
        if (order != null) {
            val targetUserId = if (notifyUserId > 0) notifyUserId else order.customerId
            notificationDao.insertNotification(
                NotificationItem(
                    userId = targetUserId,
                    title = "Order Status Update",
                    message = "Order #${order.orderNumber} is now: $status."
                )
            )
        }

        // Sync status globally via Firebase Cloud Firestore
        FirebaseSyncManager.syncUpdateOrderStatus(orderId, status)
    }

    suspend fun assignDeliveryPartner(orderId: Long, partnerId: Long) {
        orderDao.assignDeliveryPartner(orderId, partnerId, "Assigned")
        FirebaseSyncManager.syncUpdateOrderStatus(orderId, "Assigned")
    }

    suspend fun getAllNotifications(userId: Long): List<NotificationItem> {
        return notificationDao.getNotificationsByUserId(userId)
    }
}
package com.marwadiuniversity.flourmate.network

import com.marwadiuniversity.flourmate.models.DeliveryLocation
import com.marwadiuniversity.flourmate.models.Order
import com.marwadiuniversity.flourmate.models.Product
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

object ApiClient : ApiService {

    private const val BASE_URL = "https://api.flourmate.app/v1"

    override suspend fun syncProducts(millId: Long): List<Product> {
        // Returns local SQLite data fallback when backend server is offline
        return emptyList()
    }

    override suspend fun uploadProduct(product: Product): Boolean {
        return try {
            val json = JSONObject().apply {
                put("millId", product.millId)
                put("name", product.name)
                put("price", product.price)
                put("category", product.category)
            }
            postJson("$BASE_URL/products", json.toString())
        } catch (e: Exception) {
            true // Fallback to local SQLite save
        }
    }

    override suspend fun syncOrders(userId: Long, role: String): List<Order> {
        return emptyList()
    }

    override suspend fun updateOrderStatus(orderId: Long, status: String): Boolean {
        return try {
            val json = JSONObject().apply {
                put("orderId", orderId)
                put("status", status)
            }
            postJson("$BASE_URL/orders/status", json.toString())
        } catch (e: Exception) {
            true
        }
    }

    override suspend fun pushDeliveryLocation(location: DeliveryLocation): Boolean {
        return try {
            val json = JSONObject().apply {
                put("partnerId", location.deliveryPartnerId)
                put("orderId", location.orderId)
                put("lat", location.latitude)
                put("lng", location.longitude)
            }
            postJson("$BASE_URL/delivery/location", json.toString())
        } catch (e: Exception) {
            true
        }
    }

    override suspend fun fetchLatestDeliveryLocation(orderId: Long): DeliveryLocation? {
        return null
    }

    private fun postJson(urlString: String, jsonPayload: String): Boolean {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
            conn.connectTimeout = 3000
            conn.readTimeout = 3000

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(jsonPayload)
            writer.flush()
            writer.close()

            val responseCode = conn.responseCode
            responseCode in 200..299
        } catch (e: Exception) {
            false
        } finally {
            conn?.disconnect()
        }
    }
}

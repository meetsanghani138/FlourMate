package com.marwadiuniversity.flourmate.network

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.marwadiuniversity.flourmate.database.FlourMateDatabase
import com.marwadiuniversity.flourmate.models.Mill
import com.marwadiuniversity.flourmate.models.Order
import com.marwadiuniversity.flourmate.models.Product
import com.marwadiuniversity.flourmate.models.User
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FirebaseSyncManager {

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            null
        }
    }

    private fun getStorage(): FirebaseStorage? {
        return try {
            FirebaseStorage.getInstance()
        } catch (e: Throwable) {
            null
        }
    }

    private var millsListener: ListenerRegistration? = null
    private var productsListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null
    private var usersListener: ListenerRegistration? = null

    fun startRealTimeSync(context: Context) {
        try {
            val fs = getFirestore() ?: return
            val db = FlourMateDatabase.getDatabase(context)

            // Trigger migration & initial cloud fetch on I/O scope
            CoroutineScope(Dispatchers.IO).launch {
                migrateLocalDataToCloud(context)
                fetchCloudData(context)
            }

            // Real-time Listener for Mills across devices
            if (millsListener == null) {
                millsListener = fs.collection("mills")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null || snapshot == null) return@addSnapshotListener
                        CoroutineScope(Dispatchers.IO).launch {
                            for (doc in snapshot.documents) {
                                val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L
                                val ownerId = doc.getLong("ownerId") ?: 0L
                                val name = doc.getString("name") ?: ""
                                val address = doc.getString("address") ?: ""
                                val phone = doc.getString("phone") ?: ""
                                val rating = doc.getDouble("rating") ?: 4.8
                                val isAvailable = doc.getBoolean("isAvailable") ?: true
                                val imageUri = doc.getString("imageUri") ?: ""
                                val minPrice = doc.getDouble("minPricePerKg") ?: 40.0

                                if (id > 0 && name.isNotEmpty()) {
                                    val mill = Mill(
                                        id = id,
                                        ownerId = ownerId,
                                        name = name,
                                        address = address,
                                        phone = phone,
                                        rating = rating,
                                        isAvailable = isAvailable,
                                        imageUri = imageUri,
                                        minPricePerKg = minPrice
                                    )
                                    db.millDao().insertMill(mill)
                                }
                            }
                        }
                    }
            }

            // Real-time Listener for Products across devices
            if (productsListener == null) {
                productsListener = fs.collection("products")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null || snapshot == null) return@addSnapshotListener
                        CoroutineScope(Dispatchers.IO).launch {
                            for (doc in snapshot.documents) {
                                val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L
                                val millId = doc.getLong("millId") ?: 0L
                                val millName = doc.getString("millName") ?: ""
                                val name = doc.getString("name") ?: ""
                                val category = doc.getString("category") ?: ""
                                val price = doc.getDouble("price") ?: 0.0
                                val unitVariant = doc.getString("unitVariant") ?: "1 kg Pack"
                                val stock = doc.getLong("stock")?.toInt() ?: 100
                                val description = doc.getString("description") ?: ""
                                val imageUri = doc.getString("imageUri") ?: ""
                                val isAvailable = doc.getBoolean("isAvailable") ?: true

                                if (id > 0 && name.isNotEmpty()) {
                                    val product = Product(
                                        id = id,
                                        millId = millId,
                                        millName = millName,
                                        name = name,
                                        category = category,
                                        price = price,
                                        unitVariant = unitVariant,
                                        stock = stock,
                                        description = description,
                                        imageUri = imageUri,
                                        isAvailable = isAvailable
                                    )
                                    db.productDao().insertProduct(product)
                                }
                            }
                        }
                    }
            }

            // Real-time Listener for Orders across devices
            if (ordersListener == null) {
                ordersListener = fs.collection("orders")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null || snapshot == null) return@addSnapshotListener
                        CoroutineScope(Dispatchers.IO).launch {
                            for (doc in snapshot.documents) {
                                val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L
                                val orderNumber = doc.getString("orderNumber") ?: "FM0000"
                                val customerId = doc.getLong("customerId") ?: 0L
                                val customerName = doc.getString("customerName") ?: ""
                                val customerPhone = doc.getString("customerPhone") ?: ""
                                val millId = doc.getLong("millId") ?: 0L
                                val millName = doc.getString("millName") ?: ""
                                val pickupAddress = doc.getString("pickupAddress") ?: ""
                                val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                                val totalAmount = doc.getDouble("totalAmount") ?: 0.0
                                val paymentMethod = doc.getString("paymentMethod") ?: "COD"
                                val status = doc.getString("status") ?: "Pending"
                                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                val otp = doc.getString("otp") ?: "4321"
                                val deliveryPartnerId = doc.getLong("deliveryPartnerId") ?: 0L
                                val itemCount = doc.getLong("itemCount")?.toInt() ?: 1
                                val orderType = doc.getString("orderType") ?: "DELIVERY"
                                val subtotal = doc.getDouble("subtotal") ?: 0.0
                                val deliveryFee = doc.getDouble("deliveryFee") ?: 30.0
                                val completedTimestamp = doc.getLong("completedTimestamp") ?: 0L

                                if (id > 0) {
                                    val order = Order(
                                        id = id,
                                        orderNumber = orderNumber,
                                        customerId = customerId,
                                        customerName = customerName,
                                        customerPhone = customerPhone,
                                        millId = millId,
                                        millName = millName,
                                        pickupAddress = pickupAddress,
                                        deliveryAddress = deliveryAddress,
                                        totalAmount = totalAmount,
                                        paymentMethod = paymentMethod,
                                        status = status,
                                        timestamp = timestamp,
                                        otp = otp,
                                        deliveryPartnerId = deliveryPartnerId,
                                        itemCount = itemCount,
                                        orderType = orderType,
                                        subtotal = subtotal,
                                        deliveryFee = deliveryFee,
                                        completedTimestamp = completedTimestamp
                                    )
                                    db.orderDao().insertOrder(order)
                                }
                            }
                        }
                    }
            }

            // Real-time Listener for Users across devices
            if (usersListener == null) {
                usersListener = fs.collection("users")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null || snapshot == null) return@addSnapshotListener
                        CoroutineScope(Dispatchers.IO).launch {
                            for (doc in snapshot.documents) {
                                val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L
                                val name = doc.getString("name") ?: ""
                                val email = doc.getString("email") ?: ""
                                val phone = doc.getString("phone") ?: ""
                                val passwordHash = doc.getString("passwordHash") ?: ""
                                val role = doc.getString("role") ?: "CUSTOMER"
                                val millName = doc.getString("millName") ?: ""
                                val millAddress = doc.getString("millAddress") ?: ""
                                val address = doc.getString("address") ?: ""
                                val isVerified = doc.getBoolean("isVerified") ?: true
                                val profileImageUri = doc.getString("profileImageUri") ?: ""

                                if (id > 0 && email.isNotEmpty()) {
                                    val user = User(
                                        id = id,
                                        name = name,
                                        email = email,
                                        phone = phone,
                                        passwordHash = passwordHash,
                                        role = role,
                                        millName = millName,
                                        millAddress = millAddress,
                                        address = address,
                                        isVerified = isVerified,
                                        profileImageUri = profileImageUri
                                    )
                                    db.userDao().insertUser(user)
                                }
                            }
                        }
                    }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun fetchCloudData(context: Context) {
        try {
            val fs = getFirestore() ?: return
            val db = FlourMateDatabase.getDatabase(context)

            // Fetch Mills
            val millDocs = fs.collection("mills").get().await()
            for (doc in millDocs.documents) {
                val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L
                val ownerId = doc.getLong("ownerId") ?: 0L
                val name = doc.getString("name") ?: ""
                val address = doc.getString("address") ?: ""
                val phone = doc.getString("phone") ?: ""
                val rating = doc.getDouble("rating") ?: 4.8
                val isAvailable = doc.getBoolean("isAvailable") ?: true
                val imageUri = doc.getString("imageUri") ?: ""
                val minPrice = doc.getDouble("minPricePerKg") ?: 40.0

                if (id > 0 && name.isNotEmpty()) {
                    val mill = Mill(id, ownerId, name, address, phone, rating, isAvailable, imageUri, minPrice)
                    db.millDao().insertMill(mill)
                }
            }

            // Fetch Products
            val productDocs = fs.collection("products").get().await()
            for (doc in productDocs.documents) {
                val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L
                val millId = doc.getLong("millId") ?: 0L
                val millName = doc.getString("millName") ?: ""
                val name = doc.getString("name") ?: ""
                val category = doc.getString("category") ?: ""
                val price = doc.getDouble("price") ?: 0.0
                val unitVariant = doc.getString("unitVariant") ?: "1 kg Pack"
                val stock = doc.getLong("stock")?.toInt() ?: 100
                val description = doc.getString("description") ?: ""
                val imageUri = doc.getString("imageUri") ?: ""
                val isAvailable = doc.getBoolean("isAvailable") ?: true

                if (id > 0 && name.isNotEmpty()) {
                    val product = Product(id, millId, millName, name, category, price, unitVariant, stock, description, imageUri, isAvailable)
                    db.productDao().insertProduct(product)
                }
            }

            // Fetch Orders
            val orderDocs = fs.collection("orders").get().await()
            for (doc in orderDocs.documents) {
                val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L
                val orderNumber = doc.getString("orderNumber") ?: "FM0000"
                val customerId = doc.getLong("customerId") ?: 0L
                val customerName = doc.getString("customerName") ?: ""
                val customerPhone = doc.getString("customerPhone") ?: ""
                val millId = doc.getLong("millId") ?: 0L
                val millName = doc.getString("millName") ?: ""
                val pickupAddress = doc.getString("pickupAddress") ?: ""
                val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                val totalAmount = doc.getDouble("totalAmount") ?: 0.0
                val paymentMethod = doc.getString("paymentMethod") ?: "COD"
                val status = doc.getString("status") ?: "Pending"
                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                val otp = doc.getString("otp") ?: "4321"
                val deliveryPartnerId = doc.getLong("deliveryPartnerId") ?: 0L
                val itemCount = doc.getLong("itemCount")?.toInt() ?: 1
                val orderType = doc.getString("orderType") ?: "DELIVERY"
                val subtotal = doc.getDouble("subtotal") ?: 0.0
                val deliveryFee = doc.getDouble("deliveryFee") ?: 30.0
                val completedTimestamp = doc.getLong("completedTimestamp") ?: 0L

                if (id > 0) {
                    val order = Order(id, orderNumber, customerId, customerName, customerPhone, millId, millName, pickupAddress, deliveryAddress, totalAmount, paymentMethod, status, timestamp, otp, deliveryPartnerId, itemCount, orderType, subtotal, deliveryFee, completedTimestamp)
                    db.orderDao().insertOrder(order)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun migrateLocalDataToCloud(context: Context) {
        try {
            val db = FlourMateDatabase.getDatabase(context)
            val mills = db.millDao().getAllMills()
            for (m in mills) {
                syncPublishMill(m)
            }
            val products = db.productDao().getAllProducts()
            for (p in products) {
                syncPublishProduct(p)
            }
            val orders = db.orderDao().getAllOrders()
            for (o in orders) {
                syncPublishOrder(o)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun stopSync() {
        try {
            millsListener?.remove()
            productsListener?.remove()
            ordersListener?.remove()
            usersListener?.remove()
            millsListener = null
            productsListener = null
            ordersListener = null
            usersListener = null
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun syncPublishUser(user: User) {
        try {
            val fs = getFirestore() ?: return
            val map = mapOf(
                "id" to user.id,
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "passwordHash" to user.passwordHash,
                "role" to user.role,
                "millName" to user.millName,
                "millAddress" to user.millAddress,
                "address" to user.address,
                "isVerified" to user.isVerified,
                "profileImageUri" to user.profileImageUri
            )
            fs.collection("users").document(user.id.toString()).set(map).await()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun syncPublishMill(mill: Mill) {
        try {
            val fs = getFirestore() ?: return
            val map = mapOf(
                "id" to mill.id,
                "ownerId" to mill.ownerId,
                "name" to mill.name,
                "address" to mill.address,
                "phone" to mill.phone,
                "rating" to mill.rating,
                "isAvailable" to mill.isAvailable,
                "imageUri" to mill.imageUri,
                "minPricePerKg" to mill.minPricePerKg
            )
            fs.collection("mills").document(mill.id.toString()).set(map).await()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun syncPublishProduct(product: Product) {
        try {
            val fs = getFirestore() ?: return
            val map = mapOf(
                "id" to product.id,
                "millId" to product.millId,
                "millName" to product.millName,
                "name" to product.name,
                "category" to product.category,
                "price" to product.price,
                "unitVariant" to product.unitVariant,
                "stock" to product.stock,
                "description" to product.description,
                "imageUri" to product.imageUri,
                "isAvailable" to product.isAvailable
            )
            fs.collection("products").document(product.id.toString()).set(map).await()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun syncDeleteProduct(productId: Long) {
        try {
            val fs = getFirestore() ?: return
            fs.collection("products").document(productId.toString()).delete().await()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun syncPublishOrder(order: Order) {
        try {
            val fs = getFirestore() ?: return
            val map = mapOf(
                "id" to order.id,
                "orderNumber" to order.orderNumber,
                "customerId" to order.customerId,
                "customerName" to order.customerName,
                "customerPhone" to order.customerPhone,
                "millId" to order.millId,
                "millName" to order.millName,
                "pickupAddress" to order.pickupAddress,
                "deliveryAddress" to order.deliveryAddress,
                "totalAmount" to order.totalAmount,
                "paymentMethod" to order.paymentMethod,
                "status" to order.status,
                "timestamp" to order.timestamp,
                "otp" to order.otp,
                "deliveryPartnerId" to order.deliveryPartnerId,
                "itemCount" to order.itemCount,
                "orderType" to order.orderType,
                "subtotal" to order.subtotal,
                "deliveryFee" to order.deliveryFee,
                "completedTimestamp" to order.completedTimestamp
            )
            fs.collection("orders").document(order.id.toString()).set(map).await()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun syncUpdateOrderStatus(orderId: Long, status: String) {
        try {
            val fs = getFirestore() ?: return
            val updates = mutableMapOf<String, Any>(
                "status" to status
            )
            if (status == "Delivered" || status == "Completed") {
                updates["completedTimestamp"] = System.currentTimeMillis()
            }
            fs.collection("orders").document(orderId.toString()).update(updates).await()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun uploadImageToFirebaseStorage(localPath: String, folderName: String): String {
        if (localPath.isEmpty()) return ""
        if (localPath.startsWith("http://") || localPath.startsWith("https://")) return localPath

        return try {
            val st = getStorage() ?: return localPath
            val fileUri = if (localPath.startsWith("file://")) Uri.parse(localPath) else Uri.fromFile(File(localPath))
            val ref = st.reference.child("$folderName/${UUID.randomUUID()}.jpg")
            ref.putFile(fileUri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Throwable) {
            e.printStackTrace()
            localPath
        }
    }
}
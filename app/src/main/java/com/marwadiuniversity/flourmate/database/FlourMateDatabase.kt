package com.marwadiuniversity.flourmate.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.marwadiuniversity.flourmate.models.CartItem
import com.marwadiuniversity.flourmate.models.DeliveryAssignment
import com.marwadiuniversity.flourmate.models.DeliveryLocation
import com.marwadiuniversity.flourmate.models.Mill
import com.marwadiuniversity.flourmate.models.NotificationItem
import com.marwadiuniversity.flourmate.models.Order
import com.marwadiuniversity.flourmate.models.OrderItem
import com.marwadiuniversity.flourmate.models.Product
import com.marwadiuniversity.flourmate.models.User

class FlourMateDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "flourmate_app.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var INSTANCE: FlourMateDatabase? = null

        fun getDatabase(context: Context): FlourMateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = FlourMateDatabase(context.applicationContext)
                INSTANCE = instance
                instance.seedInitialData()
                instance
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                phone TEXT NOT NULL,
                passwordHash TEXT NOT NULL,
                role TEXT NOT NULL,
                millName TEXT,
                millAddress TEXT,
                address TEXT,
                isVerified INTEGER DEFAULT 1,
                profileImageUri TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE mills (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ownerId INTEGER NOT NULL,
                name TEXT NOT NULL,
                address TEXT NOT NULL,
                phone TEXT NOT NULL,
                rating REAL DEFAULT 4.8,
                isAvailable INTEGER DEFAULT 1,
                imageUri TEXT,
                minPricePerKg REAL DEFAULT 40.0,
                latitude REAL DEFAULT 22.3039,
                longitude REAL DEFAULT 70.8022
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                millId INTEGER NOT NULL,
                millName TEXT NOT NULL,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                price REAL NOT NULL,
                unitVariant TEXT DEFAULT '1 kg Pack',
                stock INTEGER DEFAULT 100,
                description TEXT,
                imageUri TEXT,
                isAvailable INTEGER DEFAULT 1
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE cart_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userId INTEGER NOT NULL,
                productId INTEGER NOT NULL,
                millId INTEGER NOT NULL,
                millName TEXT NOT NULL,
                productName TEXT NOT NULL,
                price REAL NOT NULL,
                quantity INTEGER DEFAULT 1,
                unitVariant TEXT DEFAULT '1 kg',
                imageUri TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                orderNumber TEXT NOT NULL,
                customerId INTEGER NOT NULL,
                customerName TEXT NOT NULL,
                customerPhone TEXT NOT NULL,
                millId INTEGER NOT NULL,
                millName TEXT NOT NULL,
                pickupAddress TEXT NOT NULL,
                deliveryAddress TEXT NOT NULL,
                totalAmount REAL NOT NULL,
                paymentMethod TEXT DEFAULT 'COD',
                status TEXT DEFAULT 'Pending',
                timestamp INTEGER NOT NULL,
                otp TEXT DEFAULT '4321',
                deliveryPartnerId INTEGER DEFAULT 0,
                itemCount INTEGER DEFAULT 1,
                orderType TEXT DEFAULT 'DELIVERY',
                subtotal REAL DEFAULT 0.0,
                deliveryFee REAL DEFAULT 30.0,
                completedTimestamp INTEGER DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE order_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                orderId INTEGER NOT NULL,
                productId INTEGER NOT NULL,
                productName TEXT NOT NULL,
                price REAL NOT NULL,
                quantity INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE delivery_assignments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                orderId INTEGER NOT NULL,
                deliveryPartnerId INTEGER NOT NULL,
                status TEXT DEFAULT 'Assigned',
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE delivery_locations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                deliveryPartnerId INTEGER NOT NULL,
                orderId INTEGER DEFAULT 0,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE notifications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userId INTEGER NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                isRead INTEGER DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        try { db.execSQL("ALTER TABLE orders ADD COLUMN orderType TEXT DEFAULT 'DELIVERY'") } catch (e: Exception) {}
        try { db.execSQL("ALTER TABLE orders ADD COLUMN subtotal REAL DEFAULT 0.0") } catch (e: Exception) {}
        try { db.execSQL("ALTER TABLE orders ADD COLUMN deliveryFee REAL DEFAULT 30.0") } catch (e: Exception) {}
        try { db.execSQL("ALTER TABLE orders ADD COLUMN completedTimestamp INTEGER DEFAULT 0") } catch (e: Exception) {}
        try { db.execSQL("ALTER TABLE cart_items ADD COLUMN imageUri TEXT DEFAULT ''") } catch (e: Exception) {}

        // Clean up demo/dummy mill & product data
        try { db.execSQL("DELETE FROM mills WHERE name = 'Shree Krishna Flour Mill'") } catch (e: Exception) {}
        try { db.execSQL("DELETE FROM products WHERE millName = 'Shree Krishna Flour Mill'") } catch (e: Exception) {}
        try { db.execSQL("DELETE FROM orders WHERE orderNumber = 'FM882211'") } catch (e: Exception) {}
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS mills")
        db.execSQL("DROP TABLE IF EXISTS products")
        db.execSQL("DROP TABLE IF EXISTS cart_items")
        db.execSQL("DROP TABLE IF EXISTS orders")
        db.execSQL("DROP TABLE IF EXISTS order_items")
        db.execSQL("DROP TABLE IF EXISTS delivery_assignments")
        db.execSQL("DROP TABLE IF EXISTS delivery_locations")
        db.execSQL("DROP TABLE IF EXISTS notifications")
        onCreate(db)
    }

    private fun seedInitialData() {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM users", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()

        if (count == 0) {
            // Seed Default User Accounts
            db.insert("users", null, ContentValues().apply {
                put("name", "Ramesh Bhai")
                put("email", "owner@flourmate.com")
                put("phone", "+91 98765 11111")
                put("passwordHash", "123456")
                put("role", "MILL_OWNER")
                put("millName", "My Flour Mill")
                put("millAddress", "123 Gandhi Road, Rajkot")
            })

            db.insert("users", null, ContentValues().apply {
                put("name", "Rahul Mehta")
                put("email", "customer@flourmate.com")
                put("phone", "+91 98765 43210")
                put("passwordHash", "123456")
                put("role", "CUSTOMER")
                put("address", "Flat 402, Shanti Appt, Gandhi Road, Rajkot")
            })

            db.insert("users", null, ContentValues().apply {
                put("name", "Vikram Singh")
                put("email", "delivery@flourmate.com")
                put("phone", "+91 91234 56789")
                put("passwordHash", "123456")
                put("role", "DELIVERY")
            })
        }
    }

    // DAO Instances
    fun userDao() = object : UserDao {
        override suspend fun insertUser(user: User): Long {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("name", user.name)
                put("email", user.email)
                put("phone", user.phone)
                put("passwordHash", user.passwordHash)
                put("role", user.role)
                put("millName", user.millName)
                put("millAddress", user.millAddress)
                put("address", user.address)
                put("isVerified", if (user.isVerified) 1 else 0)
                put("profileImageUri", user.profileImageUri)
            }
            return db.insert("users", null, values)
        }

        override suspend fun getUserByEmail(email: String): User? {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * FROM users WHERE email = ? LIMIT 1", arrayOf(email))
            var user: User? = null
            if (cursor.moveToFirst()) {
                user = parseUser(cursor)
            }
            cursor.close()
            return user
        }

        override suspend fun getUserById(userId: Long): User? {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * FROM users WHERE id = ? LIMIT 1", arrayOf(userId.toString()))
            var user: User? = null
            if (cursor.moveToFirst()) {
                user = parseUser(cursor)
            }
            cursor.close()
            return user
        }

        override suspend fun getUsersByRole(role: String): List<User> {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * FROM users WHERE role = ?", arrayOf(role))
            val list = mutableListOf<User>()
            while (cursor.moveToNext()) {
                list.add(parseUser(cursor))
            }
            cursor.close()
            return list
        }

        override suspend fun updateUser(user: User) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("name", user.name)
                put("phone", user.phone)
                put("address", user.address)
                put("millName", user.millName)
                put("millAddress", user.millAddress)
                put("profileImageUri", user.profileImageUri)
            }
            db.update("users", values, "id = ?", arrayOf(user.id.toString()))
        }
    }

    fun millDao() = object : MillDao {
        override suspend fun insertMill(mill: Mill): Long {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("ownerId", mill.ownerId)
                put("name", mill.name)
                put("address", mill.address)
                put("phone", mill.phone)
                put("rating", mill.rating)
                put("isAvailable", if (mill.isAvailable) 1 else 0)
                put("imageUri", mill.imageUri)
                put("minPricePerKg", mill.minPricePerKg)
                put("latitude", mill.latitude)
                put("longitude", mill.longitude)
            }
            return db.insert("mills", null, values)
        }

        override suspend fun getMillById(millId: Long): Mill? {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * FROM mills WHERE id = ? LIMIT 1", arrayOf(millId.toString()))
            var mill: Mill? = null
            if (cursor.moveToFirst()) {
                mill = parseMill(cursor)
            }
            cursor.close()
            return mill
        }

        override suspend fun getMillByOwnerId(ownerId: Long): Mill? {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * FROM mills WHERE ownerId = ? LIMIT 1", arrayOf(ownerId.toString()))
            var mill: Mill? = null
            if (cursor.moveToFirst()) {
                mill = parseMill(cursor)
            }
            cursor.close()
            return mill
        }

        override suspend fun getAllMills(): List<Mill> {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * FROM mills", null)
            val list = mutableListOf<Mill>()
            while (cursor.moveToNext()) {
                list.add(parseMill(cursor))
            }
            cursor.close()
            return list
        }

        override suspend fun updateMill(mill: Mill) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("name", mill.name)
                put("address", mill.address)
                put("phone", mill.phone)
                put("rating", mill.rating)
                put("isAvailable", if (mill.isAvailable) 1 else 0)
                put("imageUri", mill.imageUri)
                put("minPricePerKg", mill.minPricePerKg)
                put("latitude", mill.latitude)
                put("longitude", mill.longitude)
            }
            db.update("mills", values, "id = ?", arrayOf(mill.id.toString()))
        }
    }

    fun productDao() = object : ProductDao {
        override suspend fun insertProduct(product: Product): Long {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("millId", product.millId)
                put("millName", product.millName)
                put("name", product.name)
                put("category", product.category)
                put("price", product.price)
                put("unitVariant", product.unitVariant)
                put("stock", product.stock)
                put("description", product.description)
                put("imageUri", product.imageUri)
                put("isAvailable", if (product.isAvailable) 1 else 0)
            }
            return db.insert("products", null, values)
        }

        override suspend fun updateProduct(product: Product) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("millId", product.millId)
                put("millName", product.millName)
                put("name", product.name)
                put("category", product.category)
                put("price", product.price)
                put("unitVariant", product.unitVariant)
                put("stock", product.stock)
                put("description", product.description)
                put("imageUri", product.imageUri)
                put("isAvailable", if (product.isAvailable) 1 else 0)
            }
            db.update("products", values, "id = ?", arrayOf(product.id.toString()))
        }

        override suspend fun deleteProduct(product: Product) {
            deleteProductById(product.id)
        }

        override suspend fun deleteProductById(productId: Long) {
            writableDatabase.delete("products", "id = ?", arrayOf(productId.toString()))
        }

        override suspend fun getProductById(productId: Long): Product? {
            val cursor = readableDatabase.rawQuery("SELECT * FROM products WHERE id = ? LIMIT 1", arrayOf(productId.toString()))
            var p: Product? = null
            if (cursor.moveToFirst()) p = parseProduct(cursor)
            cursor.close()
            return p
        }

        override suspend fun getProductsByMillId(millId: Long): List<Product> {
            val cursor = readableDatabase.rawQuery("SELECT * FROM products WHERE millId = ? AND isAvailable = 1", arrayOf(millId.toString()))
            val list = mutableListOf<Product>()
            while (cursor.moveToNext()) list.add(parseProduct(cursor))
            cursor.close()
            return list
        }

        override suspend fun getAllProducts(): List<Product> {
            val cursor = readableDatabase.rawQuery("SELECT * FROM products WHERE isAvailable = 1", null)
            val list = mutableListOf<Product>()
            while (cursor.moveToNext()) list.add(parseProduct(cursor))
            cursor.close()
            return list
        }

        override suspend fun searchProducts(query: String): List<Product> {
            val sql = "SELECT * FROM products WHERE isAvailable = 1 AND (name LIKE ? OR category LIKE ? OR millName LIKE ?)"
            val arg = "%$query%"
            val cursor = readableDatabase.rawQuery(sql, arrayOf(arg, arg, arg))
            val list = mutableListOf<Product>()
            while (cursor.moveToNext()) list.add(parseProduct(cursor))
            cursor.close()
            return list
        }
    }

    fun cartDao() = object : CartDao {
        override suspend fun insertCartItem(item: CartItem): Long {
            val values = ContentValues().apply {
                put("userId", item.userId)
                put("productId", item.productId)
                put("millId", item.millId)
                put("millName", item.millName)
                put("productName", item.productName)
                put("price", item.price)
                put("quantity", item.quantity)
                put("unitVariant", item.unitVariant)
                put("imageUri", item.imageUri)
            }
            return writableDatabase.insert("cart_items", null, values)
        }

        override suspend fun updateCartItem(item: CartItem) {
            val values = ContentValues().apply { put("quantity", item.quantity) }
            writableDatabase.update("cart_items", values, "id = ?", arrayOf(item.id.toString()))
        }

        override suspend fun deleteCartItem(cartItemId: Long) {
            writableDatabase.delete("cart_items", "id = ?", arrayOf(cartItemId.toString()))
        }

        override suspend fun clearCart(userId: Long) {
            writableDatabase.delete("cart_items", "userId = ?", arrayOf(userId.toString()))
        }

        override suspend fun getCartItemsByUserId(userId: Long): List<CartItem> {
            val cursor = readableDatabase.rawQuery("SELECT * FROM cart_items WHERE userId = ?", arrayOf(userId.toString()))
            val list = mutableListOf<CartItem>()
            while (cursor.moveToNext()) list.add(parseCartItem(cursor))
            cursor.close()
            return list
        }

        override suspend fun getCartItemByProduct(userId: Long, productId: Long): CartItem? {
            val cursor = readableDatabase.rawQuery("SELECT * FROM cart_items WHERE userId = ? AND productId = ? LIMIT 1", arrayOf(userId.toString(), productId.toString()))
            var item: CartItem? = null
            if (cursor.moveToFirst()) item = parseCartItem(cursor)
            cursor.close()
            return item
        }
    }

    fun orderDao() = object : OrderDao {
        override suspend fun insertOrder(order: Order): Long {
            val values = ContentValues().apply {
                put("orderNumber", order.orderNumber)
                put("customerId", order.customerId)
                put("customerName", order.customerName)
                put("customerPhone", order.customerPhone)
                put("millId", order.millId)
                put("millName", order.millName)
                put("pickupAddress", order.pickupAddress)
                put("deliveryAddress", order.deliveryAddress)
                put("totalAmount", order.totalAmount)
                put("paymentMethod", order.paymentMethod)
                put("status", order.status)
                put("timestamp", order.timestamp)
                put("otp", order.otp)
                put("deliveryPartnerId", order.deliveryPartnerId)
                put("itemCount", order.itemCount)
                put("orderType", order.orderType)
                put("subtotal", order.subtotal)
                put("deliveryFee", order.deliveryFee)
                put("completedTimestamp", order.completedTimestamp)
            }
            return writableDatabase.insert("orders", null, values)
        }

        override suspend fun updateOrder(order: Order) {
            val values = ContentValues().apply {
                put("status", order.status)
                if (order.status == "Delivered" || order.status == "Completed") {
                    put("completedTimestamp", System.currentTimeMillis())
                }
            }
            writableDatabase.update("orders", values, "id = ?", arrayOf(order.id.toString()))
        }

        override suspend fun updateOrderStatus(orderId: Long, status: String) {
            val values = ContentValues().apply {
                put("status", status)
                if (status == "Delivered" || status == "Completed") {
                    put("completedTimestamp", System.currentTimeMillis())
                }
            }
            writableDatabase.update("orders", values, "id = ?", arrayOf(orderId.toString()))
        }

        override suspend fun assignDeliveryPartner(orderId: Long, partnerId: Long, status: String) {
            val values = ContentValues().apply {
                put("deliveryPartnerId", partnerId)
                put("status", status)
            }
            writableDatabase.update("orders", values, "id = ?", arrayOf(orderId.toString()))
        }

        override suspend fun getOrderById(orderId: Long): Order? {
            val cursor = readableDatabase.rawQuery("SELECT * FROM orders WHERE id = ? LIMIT 1", arrayOf(orderId.toString()))
            var order: Order? = null
            if (cursor.moveToFirst()) order = parseOrder(cursor)
            cursor.close()
            return order
        }

        override suspend fun getOrdersByCustomerId(customerId: Long): List<Order> {
            val cursor = readableDatabase.rawQuery("SELECT * FROM orders WHERE customerId = ? ORDER BY timestamp DESC", arrayOf(customerId.toString()))
            val list = mutableListOf<Order>()
            while (cursor.moveToNext()) list.add(parseOrder(cursor))
            cursor.close()
            return list
        }

        override suspend fun getOrdersByMillId(millId: Long): List<Order> {
            val cursor = readableDatabase.rawQuery("SELECT * FROM orders WHERE millId = ? ORDER BY timestamp DESC", arrayOf(millId.toString()))
            val list = mutableListOf<Order>()
            while (cursor.moveToNext()) list.add(parseOrder(cursor))
            cursor.close()
            return list
        }

        override suspend fun getOrdersByDeliveryPartnerId(partnerId: Long): List<Order> {
            val cursor = readableDatabase.rawQuery("SELECT * FROM orders WHERE deliveryPartnerId = ? ORDER BY timestamp DESC", arrayOf(partnerId.toString()))
            val list = mutableListOf<Order>()
            while (cursor.moveToNext()) list.add(parseOrder(cursor))
            cursor.close()
            return list
        }

        override suspend fun getAvailableDeliveryRequests(partnerId: Long): List<Order> {
            val sql = "SELECT * FROM orders WHERE status IN ('Accepted', 'Preparing', 'Ready for Pickup', 'Assigned', 'Pending') AND (deliveryPartnerId = 0 OR deliveryPartnerId = ?) ORDER BY timestamp DESC"
            val cursor = readableDatabase.rawQuery(sql, arrayOf(partnerId.toString()))
            val list = mutableListOf<Order>()
            while (cursor.moveToNext()) list.add(parseOrder(cursor))
            cursor.close()
            return list
        }

        override suspend fun getAllOrders(): List<Order> {
            val cursor = readableDatabase.rawQuery("SELECT * FROM orders ORDER BY timestamp DESC", null)
            val list = mutableListOf<Order>()
            while (cursor.moveToNext()) list.add(parseOrder(cursor))
            cursor.close()
            return list
        }
    }

    fun orderItemDao() = object : OrderItemDao {
        override suspend fun insertOrderItems(items: List<OrderItem>) {
            val db = writableDatabase
            for (item in items) {
                val values = ContentValues().apply {
                    put("orderId", item.orderId)
                    put("productId", item.productId)
                    put("productName", item.productName)
                    put("price", item.price)
                    put("quantity", item.quantity)
                }
                db.insert("order_items", null, values)
            }
        }

        override suspend fun getItemsByOrderId(orderId: Long): List<OrderItem> {
            val cursor = readableDatabase.rawQuery("SELECT * FROM order_items WHERE orderId = ?", arrayOf(orderId.toString()))
            val list = mutableListOf<OrderItem>()
            while (cursor.moveToNext()) {
                list.add(
                    OrderItem(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        orderId = cursor.getLong(cursor.getColumnIndexOrThrow("orderId")),
                        productId = cursor.getLong(cursor.getColumnIndexOrThrow("productId")),
                        productName = cursor.getString(cursor.getColumnIndexOrThrow("productName")),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                    )
                )
            }
            cursor.close()
            return list
        }
    }

    fun deliveryDao() = object : DeliveryDao {
        override suspend fun insertAssignment(assignment: DeliveryAssignment): Long {
            val values = ContentValues().apply {
                put("orderId", assignment.orderId)
                put("deliveryPartnerId", assignment.deliveryPartnerId)
                put("status", assignment.status)
                put("timestamp", assignment.timestamp)
            }
            return writableDatabase.insert("delivery_assignments", null, values)
        }

        override suspend fun updateAssignment(assignment: DeliveryAssignment) {
            val values = ContentValues().apply { put("status", assignment.status) }
            writableDatabase.update("delivery_assignments", values, "id = ?", arrayOf(assignment.id.toString()))
        }

        override suspend fun getAssignmentsForPartner(partnerId: Long): List<DeliveryAssignment> {
            val cursor = readableDatabase.rawQuery("SELECT * FROM delivery_assignments WHERE deliveryPartnerId = ? ORDER BY timestamp DESC", arrayOf(partnerId.toString()))
            val list = mutableListOf<DeliveryAssignment>()
            while (cursor.moveToNext()) {
                list.add(
                    DeliveryAssignment(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        orderId = cursor.getLong(cursor.getColumnIndexOrThrow("orderId")),
                        deliveryPartnerId = cursor.getLong(cursor.getColumnIndexOrThrow("deliveryPartnerId")),
                        status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
                    )
                )
            }
            cursor.close()
            return list
        }

        override suspend fun insertLocation(location: DeliveryLocation): Long {
            val values = ContentValues().apply {
                put("deliveryPartnerId", location.deliveryPartnerId)
                put("orderId", location.orderId)
                put("latitude", location.latitude)
                put("longitude", location.longitude)
                put("timestamp", location.timestamp)
            }
            return writableDatabase.insert("delivery_locations", null, values)
        }

        override suspend fun getLatestLocation(partnerId: Long): DeliveryLocation? {
            val cursor = readableDatabase.rawQuery("SELECT * FROM delivery_locations WHERE deliveryPartnerId = ? ORDER BY timestamp DESC LIMIT 1", arrayOf(partnerId.toString()))
            var loc: DeliveryLocation? = null
            if (cursor.moveToFirst()) {
                loc = DeliveryLocation(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    deliveryPartnerId = cursor.getLong(cursor.getColumnIndexOrThrow("deliveryPartnerId")),
                    orderId = cursor.getLong(cursor.getColumnIndexOrThrow("orderId")),
                    latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                    longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
                )
            }
            cursor.close()
            return loc
        }
    }

    fun notificationDao() = object : NotificationDao {
        override suspend fun insertNotification(notification: NotificationItem): Long {
            val values = ContentValues().apply {
                put("userId", notification.userId)
                put("title", notification.title)
                put("message", notification.message)
                put("timestamp", notification.timestamp)
                put("isRead", if (notification.isRead) 1 else 0)
            }
            return writableDatabase.insert("notifications", null, values)
        }

        override suspend fun getNotificationsByUserId(userId: Long): List<NotificationItem> {
            val cursor = readableDatabase.rawQuery("SELECT * FROM notifications WHERE userId = ? ORDER BY timestamp DESC", arrayOf(userId.toString()))
            val list = mutableListOf<NotificationItem>()
            while (cursor.moveToNext()) {
                list.add(
                    NotificationItem(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        userId = cursor.getLong(cursor.getColumnIndexOrThrow("userId")),
                        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        message = cursor.getString(cursor.getColumnIndexOrThrow("message")),
                        timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                        isRead = cursor.getInt(cursor.getColumnIndexOrThrow("isRead")) == 1
                    )
                )
            }
            cursor.close()
            return list
        }

        override suspend fun markAllAsRead(userId: Long) {
            val values = ContentValues().apply { put("isRead", 1) }
            writableDatabase.update("notifications", values, "userId = ?", arrayOf(userId.toString()))
        }
    }

    // Helper Cursor Parsers
    private fun parseUser(cursor: Cursor) = User(
        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
        email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
        phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
        passwordHash = cursor.getString(cursor.getColumnIndexOrThrow("passwordHash")),
        role = cursor.getString(cursor.getColumnIndexOrThrow("role")),
        millName = cursor.getString(cursor.getColumnIndexOrThrow("millName")) ?: "",
        millAddress = cursor.getString(cursor.getColumnIndexOrThrow("millAddress")) ?: "",
        address = cursor.getString(cursor.getColumnIndexOrThrow("address")) ?: "",
        isVerified = cursor.getInt(cursor.getColumnIndexOrThrow("isVerified")) == 1,
        profileImageUri = cursor.getString(cursor.getColumnIndexOrThrow("profileImageUri")) ?: ""
    )

    private fun parseMill(cursor: Cursor) = Mill(
        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
        ownerId = cursor.getLong(cursor.getColumnIndexOrThrow("ownerId")),
        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
        address = cursor.getString(cursor.getColumnIndexOrThrow("address")),
        phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
        rating = cursor.getDouble(cursor.getColumnIndexOrThrow("rating")),
        isAvailable = cursor.getInt(cursor.getColumnIndexOrThrow("isAvailable")) == 1,
        imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri")) ?: "",
        minPricePerKg = cursor.getDouble(cursor.getColumnIndexOrThrow("minPricePerKg")),
        latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
        longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"))
    )

    private fun parseProduct(cursor: Cursor) = Product(
        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
        millId = cursor.getLong(cursor.getColumnIndexOrThrow("millId")),
        millName = cursor.getString(cursor.getColumnIndexOrThrow("millName")),
        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
        category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
        price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
        unitVariant = cursor.getString(cursor.getColumnIndexOrThrow("unitVariant")) ?: "1 kg Pack",
        stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock")),
        description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: "",
        imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri")) ?: "",
        isAvailable = cursor.getInt(cursor.getColumnIndexOrThrow("isAvailable")) == 1
    )

    private fun parseCartItem(cursor: Cursor) = CartItem(
        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
        userId = cursor.getLong(cursor.getColumnIndexOrThrow("userId")),
        productId = cursor.getLong(cursor.getColumnIndexOrThrow("productId")),
        millId = cursor.getLong(cursor.getColumnIndexOrThrow("millId")),
        millName = cursor.getString(cursor.getColumnIndexOrThrow("millName")),
        productName = cursor.getString(cursor.getColumnIndexOrThrow("productName")),
        price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
        quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
        unitVariant = cursor.getString(cursor.getColumnIndexOrThrow("unitVariant")) ?: "1 kg",
        imageUri = getOptString(cursor, "imageUri", "")
    )

    private fun getOptString(cursor: Cursor, col: String, default: String): String {
        val idx = cursor.getColumnIndex(col)
        return if (idx != -1 && !cursor.isNull(idx)) cursor.getString(idx) else default
    }

    private fun getOptDouble(cursor: Cursor, col: String, default: Double): Double {
        val idx = cursor.getColumnIndex(col)
        return if (idx != -1 && !cursor.isNull(idx)) cursor.getDouble(idx) else default
    }

    private fun getOptLong(cursor: Cursor, col: String, default: Long): Long {
        val idx = cursor.getColumnIndex(col)
        return if (idx != -1 && !cursor.isNull(idx)) cursor.getLong(idx) else default
    }

    private fun parseOrder(cursor: Cursor) = Order(
        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
        orderNumber = cursor.getString(cursor.getColumnIndexOrThrow("orderNumber")),
        customerId = cursor.getLong(cursor.getColumnIndexOrThrow("customerId")),
        customerName = cursor.getString(cursor.getColumnIndexOrThrow("customerName")),
        customerPhone = cursor.getString(cursor.getColumnIndexOrThrow("customerPhone")),
        millId = cursor.getLong(cursor.getColumnIndexOrThrow("millId")),
        millName = cursor.getString(cursor.getColumnIndexOrThrow("millName")),
        pickupAddress = cursor.getString(cursor.getColumnIndexOrThrow("pickupAddress")),
        deliveryAddress = cursor.getString(cursor.getColumnIndexOrThrow("deliveryAddress")),
        totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("totalAmount")),
        paymentMethod = getOptString(cursor, "paymentMethod", "COD"),
        status = getOptString(cursor, "status", "Pending"),
        timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
        otp = getOptString(cursor, "otp", "4321"),
        deliveryPartnerId = cursor.getLong(cursor.getColumnIndexOrThrow("deliveryPartnerId")),
        itemCount = cursor.getInt(cursor.getColumnIndexOrThrow("itemCount")),
        orderType = getOptString(cursor, "orderType", "DELIVERY"),
        subtotal = getOptDouble(cursor, "subtotal", 0.0),
        deliveryFee = getOptDouble(cursor, "deliveryFee", 30.0),
        completedTimestamp = getOptLong(cursor, "completedTimestamp", 0L)
    )
}

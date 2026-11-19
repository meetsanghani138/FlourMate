package com.marwadiuniversity.flourmate.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.marwadiuniversity.flourmate.models.Product

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: Long)

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Long): Product?

    @Query("SELECT * FROM products WHERE millId = :millId AND isAvailable = 1")
    suspend fun getProductsByMillId(millId: Long): List<Product>

    @Query("SELECT * FROM products WHERE isAvailable = 1")
    suspend fun getAllProducts(): List<Product>

    @Query("SELECT * FROM products WHERE isAvailable = 1 AND (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR millName LIKE '%' || :query || '%')")
    suspend fun searchProducts(query: String): List<Product>
}

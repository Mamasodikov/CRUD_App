package com.example.crudapp.room

import androidx.paging.PagingSource
import androidx.room.*
import com.example.crudapp.models.Product

@Dao
interface ProductsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(product: Product)

    @Query("SELECT * FROM products WHERE id = 0")
    fun getAllOfflineProducts(): List<Product>

    @Delete
    suspend fun delete(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Query("SELECT * FROM products")
    fun pagingSource(): PagingSource<Int, Product>

    @Query("DELETE FROM products")
    suspend fun clearAll()
}
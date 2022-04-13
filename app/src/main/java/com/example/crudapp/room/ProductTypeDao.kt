package com.example.crudapp.room

import androidx.paging.PagingSource
import androidx.room.*
import com.example.crudapp.models.Product
import com.example.crudapp.models.ProductType

@Dao
interface ProductTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductType>)

    @Query("SELECT * FROM product_types")
    fun getAllProductType(): List<ProductType>

    @Query("DELETE FROM product_types")
    suspend fun clearAll()
}
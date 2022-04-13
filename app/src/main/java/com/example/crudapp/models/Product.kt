package com.example.crudapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    val address: String,
    val cost: Int,
    @PrimaryKey
    val created_date: Long,
    val id: Int,
    val name_uz: String,
    val product_type_id: Int
)
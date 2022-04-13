package com.example.crudapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_types")
data class ProductType(
    @PrimaryKey
    val id: Int,
    val name_ru: String,
    val name_uk: String,
    val name_uz: String
)
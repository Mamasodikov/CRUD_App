package com.example.crudapp.models

data class ProductPost(
    val address: String,
    val cost: Int,
    val created_date: Long,
    val name_uz: String,
    val product_type_id: Int
)
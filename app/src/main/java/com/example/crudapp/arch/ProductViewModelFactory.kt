package com.example.crudapp.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.crudapp.repository.Repository

class ProductViewModelFactory(private val repo: Repository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductsViewModel::class.java))
            return ProductsViewModel(repo) as T

        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}
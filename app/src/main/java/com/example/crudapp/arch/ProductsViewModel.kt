package com.example.crudapp.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.crudapp.models.Product
import com.example.crudapp.repository.Repository
import kotlinx.coroutines.flow.Flow

class ProductsViewModel(val repository: Repository) : ViewModel() {
    val productResult: Flow<PagingData<Product>> = repository.getResult().cachedIn(viewModelScope)

    fun getModelProducts(): Flow<PagingData<Product>> {
        return productResult
    }
}
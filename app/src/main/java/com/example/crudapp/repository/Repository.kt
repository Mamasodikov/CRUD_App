package com.example.crudapp.repository

import androidx.paging.*
import androidx.paging.InvalidatingPagingSourceFactory
import com.example.crudapp.api.API
import com.example.crudapp.arch.DataSource
import com.example.crudapp.models.Product
import com.example.crudapp.room.ProductDatabase
import com.example.crudapp.room.ProductsRemoteMediator
import kotlinx.coroutines.flow.Flow

class Repository(private val api: API, private val db: ProductDatabase) {

    @OptIn(ExperimentalPagingApi::class)
    fun getResult(): Flow<PagingData<Product>> {

        val pagingSourceFactory = { db.productsDao().pagingSource() }

        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            remoteMediator = ProductsRemoteMediator(db, api),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

}
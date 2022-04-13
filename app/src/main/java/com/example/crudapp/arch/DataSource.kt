package com.example.crudapp.arch

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.crudapp.api.API
import com.example.crudapp.models.Product
import retrofit2.HttpException
import java.io.IOException

class DataSource(private val service: API) : PagingSource<Int, Product>() {
    override fun getRefreshKey(state: PagingState<Int, Product>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
        return try {
            val nextPageNumber = params.key ?: 1
            val response = service.getData(nextPageNumber)
            val data = response
            LoadResult.Page(
                data = data,
                prevKey = if (nextPageNumber == 1) null else nextPageNumber - 1,
                nextKey = if (data.isEmpty()) null else nextPageNumber + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

}
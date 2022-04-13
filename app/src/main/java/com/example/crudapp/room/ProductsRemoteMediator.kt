package com.example.crudapp.room

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.crudapp.api.API
import com.example.crudapp.models.Product
import com.example.crudapp.models.RemoteKey
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class ProductsRemoteMediator (
    private val database: ProductDatabase,
    private val networkService: API
) : RemoteMediator<Int, Product>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Product>
    ): MediatorResult {

        val pageKey = getPageKeyData(state, loadType)
        val page = when(pageKey){
            is MediatorResult.Success ->{
                return pageKey
            }else ->{
                pageKey as Int
            }
        }

        return try {
            val response = networkService.getData(page)
            val isListEmpty = response.isEmpty()
            database.withTransaction {
                if(loadType == LoadType.REFRESH){
                    database.remoteDao().deleteByQuery()
                    database.productsDao().clearAll()
                }

                val prevKey = if (page == 1) null else page -1
                val nextKey = if(isListEmpty) null else page +1

                val keys = response.map {
                    RemoteKey(it.id.toString(), prevKey, nextKey)
                }

                database.productsDao().insertAll(response)
                database.remoteDao().insertOrReplace(keys)
            }
            return MediatorResult.Success(endOfPaginationReached = isListEmpty)
        }
        catch (e:IOException){
            MediatorResult.Error(e)
        }
        catch (e:HttpException){
            MediatorResult.Error(e)
        }

    }

    private suspend fun getPageKeyData(state: PagingState<Int, Product>, loadType: LoadType): Any{
        return when(loadType){
            LoadType.REFRESH->{
                val remoteKeys = getCurrentPosition(state)
                val current = remoteKeys?.nextKey?.minus(1)
                if(current!=null){
                    return current
                }else{
                    1
                }
            }

            LoadType.PREPEND->{

                val remoteKeys = getFirstPosition(state)
                val prevKey = remoteKeys?.prevKey?:
                MediatorResult.Success(endOfPaginationReached = true)
                prevKey
            }

            LoadType.APPEND->{

                val remoteKeys = getLastPosition(state)
                val nextKey = remoteKeys?.nextKey
                return if(nextKey!=null) nextKey else MediatorResult.Success(endOfPaginationReached = false)
            }


        }
    }

    private suspend fun getLastPosition(state: PagingState<Int, Product>): RemoteKey? {

        return state.pages.lastOrNull{
            it.data.isNotEmpty() }
            ?.data?.lastOrNull()
            .let{
                    product ->
                database.remoteDao().remoteKeyByQuery(product?.id!!)
            }

    }

    private suspend fun getFirstPosition(state: PagingState<Int, Product>): RemoteKey? {
        return state.pages
            .firstOrNull{ it.data.isNotEmpty() }
            ?.data?.firstOrNull()
            .let{
                    product ->
                database.remoteDao().remoteKeyByQuery(product?.id!!)
            }
    }

    private suspend fun getCurrentPosition(state: PagingState<Int, Product>): RemoteKey?{
        return  state.anchorPosition?.let {
            state.closestItemToPosition(it)?.id?.let {id->
                database.remoteDao().remoteKeyByQuery(id)
            }
        }
    }
}
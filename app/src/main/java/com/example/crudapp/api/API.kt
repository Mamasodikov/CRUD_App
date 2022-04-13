package com.example.crudapp.api

import com.example.crudapp.models.Product
import com.example.crudapp.models.ProductPost
import com.example.crudapp.models.ProductType
import com.example.crudapp.utils.Credentials.Companion.BASE_URL
import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


interface API {
    @GET("product")
    suspend fun getData(
        @Query("page") page: Int,
        @Query("perPage") size: Int = 10
    ): List<Product>

    @POST("product")
    suspend fun postData(@Body product: ProductPost)

    @PUT("product")
    suspend fun updateData(@Body product: Product)

    @DELETE("product/{id}")
    suspend fun deleteData(@Path("id") prodID: Int): ResponseBody

    @GET("product/get-product-types")
    suspend fun getProductTypes ():List<ProductType>

    companion object {

//    var gson = GsonBuilder()
//        .setLenient()
//        .create()

        operator fun invoke(): API = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(API::class.java)
    }

}
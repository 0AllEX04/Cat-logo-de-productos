package com.example.catlogodeproductos.network

import com.example.catlogodeproductos.model.ProductsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductApi {

    @GET("products")
    fun getProducts(
        @Query("limit") limit: Int = 10,
        @Query("skip") skip: Int = 0
    ): Call<ProductsResponse>
}
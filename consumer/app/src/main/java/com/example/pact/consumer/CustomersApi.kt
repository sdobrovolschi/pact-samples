package com.example.pact.consumer

import retrofit2.http.GET
import retrofit2.http.Path

interface CustomersApi {

    @GET("/customers/{id}")
    suspend fun find(@Path("id") customerId: String): Customer
}

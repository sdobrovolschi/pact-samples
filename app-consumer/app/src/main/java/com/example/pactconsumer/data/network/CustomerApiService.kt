package com.example.pactconsumer.data.network

import com.example.pactconsumer.data.model.CustomerResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface CustomerApiService {

    @GET("/customers/{id}")
    suspend fun getCustomer(@Path("id") customerId: String): CustomerResponse
}
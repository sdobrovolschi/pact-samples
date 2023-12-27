package com.example.pactconsumer

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody
import au.com.dius.pact.consumer.dsl.PactBuilder
import au.com.dius.pact.consumer.junit5.PactConsumerTest
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.V4Pact
import au.com.dius.pact.core.model.annotations.Pact
import com.example.pactconsumer.data.model.CustomerResponse
import com.example.pactconsumer.data.network.CustomerApiService
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.assertj.core.api.Assertions.assertThat

@PactConsumerTest
@PactTestFor(providerName = "provider")
class ConsumerTest {

    @Pact(consumer = "consumer")
    fun customer(builder: PactBuilder): V4Pact {
        return builder
            .given("a customer with id 1")
            .expectsToReceiveHttpInteraction("a request for a customer") { httpBuilder ->
                httpBuilder.withRequest { request ->
                    request
                        .method("GET")
                        .path("/customers/1")
                }
                    .willRespondWith { response ->
                        response
                            .status(200)
                            .header("Content-Type", "application/json")
                            .body(newJsonBody { customer ->
                                customer
                                    .stringValue("customerId", "1")
                                    .stringValue("name", "John Snow")
                                    .stringValue("email", "jsnow@test.com")
                            }
                                .build())
                    }
            }
            .toPact()
    }

    @Test
    @PactTestFor(pactMethod = "customer")
    fun testCustomer(mockServer: MockServer) {
        val okHttpClient = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(mockServer.getUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val customerServiceApi = retrofit.create(CustomerApiService::class.java)

        runBlocking {
            assertThat(customerServiceApi.getCustomer("1"))
                .usingRecursiveAssertion()
                .isEqualTo(
                    CustomerResponse(
                        customerId = "1",
                        name = "John Snow",
                        email = "jsnow@test.com"
                    )
                )
        }
    }
}
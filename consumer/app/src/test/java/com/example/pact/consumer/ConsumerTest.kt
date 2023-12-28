package com.example.pact.consumer

import android.annotation.SuppressLint
import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody
import au.com.dius.pact.consumer.dsl.PactBuilder
import au.com.dius.pact.consumer.junit5.PactConsumerTest
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.V4Pact
import au.com.dius.pact.core.model.annotations.Pact
import kotlinx.coroutines.test.runTest
import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.assertj.core.api.Assertions.assertThat
import java.nio.charset.StandardCharsets.UTF_8
@PactConsumerTest
@PactTestFor(providerName = "provider")
class ConsumerTest {

    private lateinit var httpClient: OkHttpClient
    private lateinit var customersApi: CustomersApi

    @BeforeEach
    fun setUp(mockServer: MockServer) {
        httpClient = OkHttpClient.Builder().build()

        val retrofit = Retrofit.Builder()
            .client(httpClient)
            .baseUrl(mockServer.getUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        customersApi = retrofit.create(CustomersApi::class.java)
    }

    @Pact(consumer = "consumer")
    fun customer(builder: PactBuilder): V4Pact {
        return builder.given("a customer with id 1")
            .expectsToReceiveHttpInteraction("a request for a customer") { httpBuilder ->
                httpBuilder.withRequest { request ->
                    request.method("GET").path("/customers/1")
                }.willRespondWith { response ->
                        response.status(200).header("Content-Type", "application/json")
                            .body(newJsonBody { customer ->
                                customer.stringValue("customerId", "1")
                                    .`object`("fullName") { fullName->
                                        fullName.stringValue("firstName", "John")
                                            .stringValue("lastName", "Snow")
                                    }
                                    .stringValue("name", "John Snow")
                                    .stringValue("email", "jsnow@test.com")
                            }.build())
                    }
            }.toPact()
    }

    @Test
    @Tag("base")
    @PactTestFor(pactMethod = "customer")
    fun testCustomer(mockServer: MockServer) = runTest {
        val customer = customersApi.find("1");

        assertThat(customer)
            .usingRecursiveAssertion()
            .isEqualTo(
                Customer(
                    customerId = "1",
                    name = "John Snow",
                    email = "jsnow@test.com"
                )
            )
    }

    @Test
    @Tag("feat.customer-enrichment")
    @PactTestFor(pactMethod = "customer")
    fun customerInfoEnriched(mockServer: MockServer) = runTest {
        val request = Request.Builder()
            .method("GET", null)
            .url("${mockServer.getUrl()}/customers/1")
            .addHeader("Content-Type", "application/json")
            .build()

        val response = httpClient.newCall(request).execute()

        assertThatJson(String(response.body()!!.bytes(), UTF_8))
            .whenIgnoringPaths("$.name")
            .inPath("$")
            .isEqualTo("{\n  \"customerId\": \"1\",\n  \"fullName\": {\n    \"firstName\": \"John\",\n    \"lastName\": \"Snow\"\n  },\n  \"email\": \"jsnow@test.com\"\n}")
    }
}

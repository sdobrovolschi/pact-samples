package com.example.pactconsumer

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
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets.UTF_8

@PactConsumerTest
@PactTestFor(providerName = "provider")
class ConsumerTest {

    private lateinit var okHttpClient: OkHttpClient

    @BeforeEach
    fun setUp() {
        okHttpClient = OkHttpClient.Builder().build()
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
                                    .stringValue("name", "John Snow")
                                    .stringValue("email", "jsnow@test.com")
                            }.build())
                    }
            }.toPact()
    }

    @SuppressLint("CheckResult")
    @Test
    @PactTestFor(pactMethod = "customer")
    fun testCustomer(mockServer: MockServer) = runTest {
        val request = Request.Builder()
            .method("GET", null)
            .url("${mockServer.getUrl()}/customers/1")
            .addHeader("Content-Type", "application/json")
            .build()

        val response = okHttpClient.newCall(request).execute()

        assertThatJson(String(response.body()!!.bytes(), UTF_8))
            .inPath("$")
            .isEqualTo("{\n  \"customerId\": \"1\",\n  \"name\": \"John Snow\",\n  \"email\": \"jsnow@test.com\"\n}")
    }
}
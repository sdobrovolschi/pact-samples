package com.example.pact.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.optional;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@PactConsumerTest
@PactTestFor(providerName = "provider")
class ConsumerApplicationTests {

	BasicJsonTester json = new BasicJsonTester(getClass());
	RestClient client;

	@BeforeEach
	void setUp(MockServer mockServer) {
		client = RestClient.create(mockServer.getUrl());
	}

	@Pact(consumer = "consumer")
	V4Pact customer(PactBuilder builder) {
		return builder
				.given("a customer with id 1")
				.expectsToReceiveHttpInteraction("a request for a customer", http -> http
						.withRequest(request -> request
								.method("GET")
								.path("/customers/1"))
						.willRespondWith(response -> response
								.status(200)
								.header("Content-Type", "application/json")
								.body(newJsonBody(customer -> customer
										.stringValue("customerId", "1")
										.stringValue("name", "John Snow")
										.stringValue("email", "jsnow@test.com"))
										.build())))
				.toPact();
	}

	@Test
	@PactTestFor(pactMethod = "customer")
	void findingKnown() {
		var body = client.get().uri("/customers/1")
				.retrieve()
				.body(String.class);

		assertThat(json.from(body))
				.extractingJsonPathStringValue("@.customerId")
				.isEqualTo("1");
	}
}

package com.example.pact.provider;

import java.util.Optional;

public interface FindCustomerUseCase {

    Optional<Customer> find(String customerId);
}

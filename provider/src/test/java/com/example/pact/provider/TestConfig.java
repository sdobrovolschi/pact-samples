package com.example.pact.provider;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TestConfig {

    @MockBean
    FindCustomerUseCase findCustomerUseCase;
}

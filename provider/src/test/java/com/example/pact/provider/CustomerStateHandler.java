package com.example.pact.provider;

import au.com.dius.pact.provider.junitsupport.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.mockito.Mockito.when;

@Component
class CustomerStateHandler implements StateHandler {

    @Autowired
    FindCustomerUseCase useCase;

    @State("a customer with id 1")
    void customer() {
        when(useCase.find("1"))
                .thenReturn(Optional.of(new Customer("1", "John Snow", "jsnow@test.com")));
    }
}

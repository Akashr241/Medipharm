package com.example.demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DemoApplicationTests {

    @Test
    void homeReturnsWelcomeMessage() {
        DemoApplication application = new DemoApplication();

        assertEquals("Hello World from Spring Boot!", application.home());
    }
}

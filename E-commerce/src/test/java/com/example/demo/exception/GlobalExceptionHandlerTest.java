package com.example.demo.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ModelAttribute;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidationExceptionReturnsBadRequest() throws Exception {
        Method method = DummyController.class.getDeclaredMethod("save", DummyRequest.class);
        MethodArgumentNotValidException exception = createValidationException(method);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception);

        assertResponse(response, HttpStatus.BAD_REQUEST, "Name is required");
    }

    @Test
    void handleResourceNotFoundExceptionReturnsNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("Product not found"));

        assertResponse(response, HttpStatus.NOT_FOUND, "Product not found");
    }

    @Test
    void handleInvalidOrderStatusExceptionReturnsBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidOrderStatusException(
                new InvalidOrderStatusException("Order status is invalid"));

        assertResponse(response, HttpStatus.BAD_REQUEST, "Order status is invalid");
    }

    @Test
    void handlePaymentExceptionReturnsBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handlePaymentException(
                new PaymentException("Payment failed"));

        assertResponse(response, HttpStatus.BAD_REQUEST, "Payment failed");
    }

    @Test
    void handleGlobalExceptionReturnsInternalServerError() {
        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(
                new RuntimeException("Something went wrong"));

        assertResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
    }

    private void assertResponse(ResponseEntity<ErrorResponse> response, HttpStatus expectedStatus,
            String expectedMessage) {
        assertEquals(expectedStatus.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(expectedMessage, response.getBody().getMessage());
        assertEquals(expectedStatus.value(), response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    private MethodArgumentNotValidException createValidationException(Method method) {
        DummyRequest request = new DummyRequest();
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "dummyRequest");
        bindingResult.rejectValue("name", "NotBlank", "Name is required");

        return new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, 0), bindingResult);
    }

    static class DummyController {
        public void save(@ModelAttribute DummyRequest request) {
        }
    }

    static class DummyRequest {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

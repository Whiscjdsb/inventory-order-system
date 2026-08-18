package com.example.inventorypractice.exception;

import com.example.inventorypractice.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler =
            new GlobalExceptionHandler();

    @Test
    void shouldHideUnexpectedExceptionDetails() {
        ResponseEntity<ApiResponse<Void>> response =
                exceptionHandler.handleUnexpectedException(
                        new RuntimeException("database password leaked")
                );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiResponse<Void> body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.getCode());
        assertEquals("服务器内部错误", body.getMessage());
        assertNull(body.getData());
    }
}

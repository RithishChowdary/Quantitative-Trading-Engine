package com.rithish.trading.exceptions;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * Handles invalid input such as:
     * empty symbol
     * null symbol
     * invalid parameters
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * Handles failures while downloading historical market data.
     */
    @ExceptionHandler(HistoricalDataDownloadException.class)
    public ResponseEntity<Map<String, Object>> handleHistoricalDataDownloadException(
            HistoricalDataDownloadException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Historical Data Not Found",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * Handles failures during backtest execution.
     */
    @ExceptionHandler(BacktestExecutionException.class)
    public ResponseEntity<Map<String, Object>> handleBacktestExecutionException(
            BacktestExecutionException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Backtest Execution Failed",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * Final safety net for unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred.",
                request.getRequestURI()
        );
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            String path) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "timestamp",
                LocalDateTime.now()
        );

        response.put(
                "status",
                status.value()
        );

        response.put(
                "error",
                error
        );

        response.put(
                "message",
                message
        );

        response.put(
                "path",
                path
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}
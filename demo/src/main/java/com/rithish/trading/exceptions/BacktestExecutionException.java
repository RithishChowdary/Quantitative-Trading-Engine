package com.rithish.trading.exceptions;

public class BacktestExecutionException
        extends RuntimeException {

    public BacktestExecutionException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}
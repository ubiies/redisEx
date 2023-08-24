package com.example.redisex.global.exception;

public enum ErrorCode {
    REDIS_ERROR("An error occurred while connecting to Redis."),
    INVALID_JWT("Invalid JWT token.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

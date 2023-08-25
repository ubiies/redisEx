package com.example.redisex.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    REDIS_ERROR(HttpStatus.BAD_REQUEST,"An error occurred while connecting to Redis."),
    INVALID_JWT(HttpStatus.BAD_REQUEST, "Invalid JWT token."),
    DUPLICATED_USER_NAME(HttpStatus.CONFLICT, "user name is duplicated"),
    DIFF_PASSWORD_CHECK(HttpStatus.BAD_REQUEST, "password check is different with password"),
    PAGE_NUMBER_OUT_OF_BOUNDS(HttpStatus.BAD_REQUEST, "page number is wrong");

    private HttpStatus status;
    private final String message;
}

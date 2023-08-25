package com.example.redisex.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseException extends RuntimeException {
    private ErrorCode errorCode;
    private String message;

    @Override
    public String toString() {
        if(message == null) return errorCode.getMessage();
        return String.format("%s. %s", errorCode.getMessage(), message);
    }
}

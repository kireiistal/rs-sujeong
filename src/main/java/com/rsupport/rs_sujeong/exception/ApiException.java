package com.rsupport.rs_sujeong.exception;

public class ApiException extends RuntimeException {
    public ApiException(String errorCode, String message) {
        super(message);
    }
}

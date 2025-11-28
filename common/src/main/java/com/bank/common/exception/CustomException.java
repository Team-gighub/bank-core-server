package com.bank.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 부모 RuntimeException.message에도 저장
        this.errorCode = errorCode;
    }
}
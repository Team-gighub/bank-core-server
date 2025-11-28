package com.bank.exception;

import com.bank.common.exception.CustomException;
import com.bank.common.exception.ErrorCode;
import com.bank.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 서비스 로직에서 발생한 CustomException 처리
     *
     * @param e CustomException
     * @return ApiResponse.failure()
     */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {

        ErrorCode errorCode = e.getErrorCode();

        log.error("[CustomException] {} - {}", errorCode.getCode(), errorCode.getMessage(), e);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * JSON 파싱 실패
     *
     * @param e
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiResponse<Void>> handleJsonParseException(HttpMessageNotReadableException e) {
        ErrorCode error = ErrorCode.INVALID_JSON;
        log.error("[JSON Parse Error] {}", e.getMessage());
        return ResponseEntity
                .status(error.getStatus())
                .body(ApiResponse.failure(error.getCode(), error.getMessage()));
    }

    /**
     * 지원하지 않는 HTTP Method
     *
     * @param e
     * @return
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        ErrorCode error = ErrorCode.METHOD_NOT_ALLOWED;
        log.error("[Method Not Allowed] {}", e.getMessage());
        return ResponseEntity
                .status(error.getStatus())
                .body(ApiResponse.failure(error.getCode(), error.getMessage()));
    }

    /**
     * 처리되지 않은 모든 예외 (Catch-All)
     *
     * @param e Exception
     * @return
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {

        log.error("[Unexpected Exception] {}", e.getMessage(), e);

        ErrorCode error = ErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(error.getStatus())
                .body(ApiResponse.failure(error.getCode(), error.getMessage()));
    }
}
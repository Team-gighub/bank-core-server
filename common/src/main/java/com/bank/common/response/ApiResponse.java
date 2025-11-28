package com.bank.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final boolean isSuccess;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final ErrorResponse error;

    /**
     *
     *
     * @param isSuccess
     * @param data
     * @param error
     */
    private ApiResponse(boolean isSuccess, T data, ErrorResponse error) {
        this.isSuccess = isSuccess;
        this.data = data;
        this.error = error;
    }

    // 성공(데이터 있음)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    // 성공(데이터 없음)
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, null);
    }

    /**
     *
     *
     * @param code
     * @param message
     * @return
     * @param <T>
     */
    // 실패
    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorResponse(code, message));
    }
}
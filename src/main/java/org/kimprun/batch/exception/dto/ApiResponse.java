package org.kimprun.batch.exception.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponse<T> {

    private final int status;
    private final String message;
    private final T data;
    private final String detail;

    private ApiResponse(int status, String message, T data, String detail) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.detail = detail;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Success", data, null);
    }

    public static <T> ApiResponse<T> success(HttpStatus status, T data) {
        return new ApiResponse<>(status.value(), "Success", data, null);
    }

    public static <T> ApiResponse<T> error(int status, String message, String detail) {
        return new ApiResponse<>(status, message, null, detail);
    }

    public boolean isSuccess() {
        return this.message.equals("Success");
    }
}
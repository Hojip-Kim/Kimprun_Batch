package org.kimprun.batch.exception.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponseDTO {

    private final int status;            // 표준 error status code
    private final String error;          // ex) BAD_REQUEST, NOT_FOUND, INTERNAL_SERVER_ERROR
    private final String message;        // 메시지
    private final String trace;          // exception stack trace

    public ErrorResponseDTO(HttpStatus httpStatus, String message, String trace) {
        this.status = httpStatus.value();
        this.error = httpStatus.name();
        this.message = message;
        this.trace = trace;
    }


}

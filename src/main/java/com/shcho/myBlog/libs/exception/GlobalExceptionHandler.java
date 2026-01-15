package com.shcho.myBlog.libs.exception;

import com.shcho.myBlog.libs.dto.ExceptionResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // CustomException 처리 - ErrorCode에 정의된 예외 반환
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionResponseDto> handleCustomException(CustomException e) {
        log.error("CustomException 발생: {}", e.getMessage());
        var errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ExceptionResponseDto.ofCode(
                        errorCode.getHttpStatus(),
                        errorCode.getMessage(),
                        errorCode.getCode()
                ));
    }

    // RuntimeException 처리 - 서버 내부 오류 반환
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponseDto> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException 발생: ", e);
        var errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ExceptionResponseDto.ofCode(
                        errorCode.getHttpStatus(),
                        errorCode.getMessage(),
                        errorCode.getCode()
                ));
    }

    // @Valid, @Validated 관련 Validation 오류 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDto> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation 오류 발생: {}", e.getMessage());

        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = (fieldError != null) ? fieldError.getDefaultMessage() : "잘못된 요청입니다.";
        String field = (fieldError != null) ? fieldError.getField() : null;

        return ResponseEntity
                .badRequest()
                .body(ExceptionResponseDto.ofValidation(400, message, field));
    }
}

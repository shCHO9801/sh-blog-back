package com.shcho.myBlog.libs.dto;

public record ExceptionResponseDto(
        Integer status,
        String message,
        String code,
        String field
) {
    public static ExceptionResponseDto ofFull(Integer status, String message, String code, String field) {
        return new ExceptionResponseDto(status, message, code, field);
    }

    public static ExceptionResponseDto ofCode(Integer status, String message, String code) {
        return new ExceptionResponseDto(status, message, code, null);
    }

    public static ExceptionResponseDto ofValidation(Integer status, String message, String field) {
        return new ExceptionResponseDto(status, message, null, field);
    }

    public static ExceptionResponseDto ofSimple(Integer status, String message) {
        return new ExceptionResponseDto(status, message, null, null);
    }
}

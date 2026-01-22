package com.shcho.myBlog.libs.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    /* 400 BAD_REQUEST */

    /* 401 UNAUTHORIZED */
    INVALID_USERNAME_OR_PASSWORD(401, "AUTH_001", "아이디 또는 비밀번호가 올바르지 않습니다."),
    AUTH_REQUIRED(401, "AUTH_002", "인증이 필요합니다."),

    /* 403 FORBIDDEN */

    /* 404 NOT_FOUND */
    USER_NOT_FOUND(404, "USER_001", "유저를 찾을 수 없습니다."),
    BLOG_NOT_FOUND(404, "BLOG_001", "블로그를 찾을 수 없습니다."),

    /* 409 Conflict*/
    DUPLICATED_USERNAME(409, "USER_003", "이미 사용 중인 아이디 입니다."),
    DUPLICATED_EMAIL(409, "USER_004", "이미 사용 중인 이메일입니다."),
    DUPLICATED_NICKNAME(409, "USER_005", "이미 사용 중인 닉네임입니다."),

    /* 500 INTERNAL_SERVER_ERROR */
    INTERNAL_SERVER_ERROR(500, "COMMON_500", "서버 오류가 발생했습니다."),
    JWT_KEY_ERROR(500, "AUTH_500", "JWT 키가 유효하지 않습니다.");

    private final Integer httpStatus;
    private final String code;
    private final String message;
}

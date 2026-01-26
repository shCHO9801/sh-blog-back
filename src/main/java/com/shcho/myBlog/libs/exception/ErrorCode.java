package com.shcho.myBlog.libs.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    /* 400 BAD_REQUEST */
    CATEGORY_PARENT_DEPTH_EXCEEDED(400, "CATEGORY_004", "카테고리는 2단계까지만 생성할 수 있습니다."),
    CATEGORY_CANNOT_HAVE_CHILDREN(400, "CATEGORY_005", "미분류 카테고리는 하위 카테고리를 가질 수 없습니다."),

    /* 401 UNAUTHORIZED */
    INVALID_USERNAME_OR_PASSWORD(401, "AUTH_001", "아이디 또는 비밀번호가 올바르지 않습니다."),
    AUTH_REQUIRED(401, "AUTH_002", "인증이 필요합니다."),

    /* 403 FORBIDDEN */
    CATEGORY_FORBIDDEN(403, "CATEGORY_003", "해당 카테고리에 대한 권한이 없습니다."),

    /* 404 NOT_FOUND */
    USER_NOT_FOUND(404, "USER_001", "유저를 찾을 수 없습니다."),
    BLOG_NOT_FOUND(404, "BLOG_001", "블로그를 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(404, "CATEGORY_002", "카테고리를 찾을 수 없습니다."),

    /* 409 Conflict*/
    DUPLICATED_USERNAME(409, "USER_003", "이미 사용 중인 아이디 입니다."),
    DUPLICATED_EMAIL(409, "USER_004", "이미 사용 중인 이메일입니다."),
    DUPLICATED_NICKNAME(409, "USER_005", "이미 사용 중인 닉네임입니다."),
    DUPLICATED_CATEGORY_NAME(409, "CATEGORY_001", "이미 사용 중인 카테고리명 입니다."),

    /* 500 INTERNAL_SERVER_ERROR */
    INTERNAL_SERVER_ERROR(500, "COMMON_500", "서버 오류가 발생했습니다."),
    JWT_KEY_ERROR(500, "AUTH_500", "JWT 키가 유효하지 않습니다.");

    private final Integer httpStatus;
    private final String code;
    private final String message;
}

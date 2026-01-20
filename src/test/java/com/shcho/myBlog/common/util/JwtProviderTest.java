package com.shcho.myBlog.common.util;

import com.shcho.myBlog.libs.exception.CustomException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static com.shcho.myBlog.libs.exception.ErrorCode.JWT_KEY_ERROR;
import static com.shcho.myBlog.user.entity.Role.USER;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtProvider Unit Test")
class JwtProviderTest {
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        String TEST_SECRET = "testSecret123!!!123!!!testSecret123!!!123!!!";
        ReflectionTestUtils.setField(jwtProvider, "secretKey", TEST_SECRET);
        jwtProvider.init();
    }

    @Test
    @DisplayName("토큰 생성 성공")
    void createTokenSuccess() {
        // when
        String token = jwtProvider.createToken("testUser", USER);

        // then
        assertNotNull(token);
    }

    @Test
    @DisplayName("토큰에서 Username 추출 성공")
    void getUsernameFromTokenSuccess() {
        // given
        String token = jwtProvider.createToken("testUser", USER);

        // when
        String username = jwtProvider.getUsername(token);

        // then
        assertEquals("testUser", username);
    }

    @Test
    @DisplayName("토큰 유효성 검사 - 유효한 토큰")
    void validateTokenSuccess() {
        // given
        String token = jwtProvider.createToken("testUser", USER);

        // then
        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
    @DisplayName("토큰 유효성 검사 - 만료된 토큰")
    void validateTokenExpired() {
        // given
        SecretKey key = (SecretKey) ReflectionTestUtils.getField(jwtProvider, "key");

        Date expired = new Date(System.currentTimeMillis() - 1000);

        String token = Jwts.builder()
                .subject("expiredUser")
                .claim("role", USER.name())
                .issuedAt(new Date())
                .expiration(expired)
                .signWith(key)
                .compact();

        // when
        boolean isValid = jwtProvider.validateToken(token);

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("JWT 초기화 실패 - 시크릿 키가 짧음")
    void initFailedDueToShortKey() {
        // given
        JwtProvider tempProvider = new JwtProvider();
        ReflectionTestUtils.setField(tempProvider, "secretKey", "shortKey");

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                tempProvider::init);

        assertEquals(JWT_KEY_ERROR, exception.getErrorCode());
    }
}
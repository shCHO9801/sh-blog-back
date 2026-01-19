package com.shcho.myBlog.user.service;

import com.shcho.myBlog.libs.exception.CustomException;
import com.shcho.myBlog.user.dto.UserSignUpRequestDto;
import com.shcho.myBlog.user.entity.User;
import com.shcho.myBlog.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.shcho.myBlog.libs.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("User Service Unit Test")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    @Test
    @DisplayName("회원가입 성공")
    void signUpUserSuccess() {
        // given
        UserSignUpRequestDto requestDto = createTestRequest();

        when(userRepository.existsByUsername(requestDto.username())).thenReturn(false);
        when(userRepository.existsByNickname(requestDto.nickname())).thenReturn(false);
        when(userRepository.existsByEmail(requestDto.email())).thenReturn(false);

        when(passwordEncoder.encode(requestDto.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        User result = userService.signUp(requestDto);

        // then

        // return value 검증
        assertEquals(requestDto.username(), result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(requestDto.nickname(), result.getNickname());
        assertEquals(requestDto.email(), result.getEmail());

        // save 인자 검증
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(requestDto.username(), savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(requestDto.nickname(), savedUser.getNickname());
        assertEquals(requestDto.email(), savedUser.getEmail());

        verify(passwordEncoder, times(1)).encode(requestDto.password());
    }

    @Test
    @DisplayName("회원가입 실패 - Username 중복")
    void signUpUserFailedDuplicatedUsername() {
        // given
        UserSignUpRequestDto requestDto = createTestRequest();
        when(userRepository.existsByUsername(requestDto.username())).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.signUp(requestDto));
        assertEquals(DUPLICATED_USERNAME, exception.getErrorCode());

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).existsByNickname(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 실패 - Email 중복")
    void signUpUserFailedDuplicatedEmail() {
        // given
        UserSignUpRequestDto requestDto = createTestRequest();
        when(userRepository.existsByUsername(requestDto.username())).thenReturn(false);
        when(userRepository.existsByEmail(requestDto.email())).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.signUp(requestDto));
        assertEquals(DUPLICATED_EMAIL, exception.getErrorCode());

        verify(userRepository, never()).existsByNickname(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 실패 - Nickname 중복")
    void signUpUserFailedDuplicatedNickname() {
        // given
        UserSignUpRequestDto requestDto = createTestRequest();
        when(userRepository.existsByUsername(requestDto.username())).thenReturn(false);
        when(userRepository.existsByEmail(requestDto.email())).thenReturn(false);
        when(userRepository.existsByNickname(requestDto.nickname())).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.signUp(requestDto));
        assertEquals(DUPLICATED_NICKNAME, exception.getErrorCode());

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    private UserSignUpRequestDto createTestRequest() {
        return new UserSignUpRequestDto("newUser", "password", "newNickname", "new@email.com");
    }

}
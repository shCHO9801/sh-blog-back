package com.shcho.myBlog.user.service;

import com.shcho.myBlog.category.repository.CategoryRepository;
import com.shcho.myBlog.common.service.MinioService;
import com.shcho.myBlog.common.util.JwtProvider;
import com.shcho.myBlog.libs.exception.CustomException;
import com.shcho.myBlog.user.dto.*;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static com.shcho.myBlog.common.entity.UploadType.PROFILE_IMAGE;
import static com.shcho.myBlog.libs.exception.ErrorCode.*;
import static com.shcho.myBlog.user.entity.Role.USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("User Service Unit Test")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private MinioService minioService;
    @InjectMocks
    private UserService userService;

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

    @Test
    @DisplayName("로그인 성공")
    void signInUserSuccess() {
        // given
        String rawPassword = "password";

        User user = User.builder()
                .username("existsUsername")
                .nickname("test")
                .email("test@email.com")
                .password("encodedPassword")
                .role(USER)
                .build();

        UserSignInRequestDto requestDto =
                createSignInRequest(user.getUsername(), rawPassword);

        when(userRepository.findByUsername(requestDto.username()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, user.getPassword()))
                .thenReturn(true);

        // when
        User signinUser = userService.signIn(requestDto);

        // then
        assertEquals(signinUser.getUsername(), user.getUsername());
        assertEquals(signinUser.getNickname(), user.getNickname());
        assertEquals(signinUser.getEmail(), user.getEmail());
        assertEquals(signinUser.getPassword(), user.getPassword());

        verify(userRepository, times(1)).findByUsername(requestDto.username());
        verify(passwordEncoder, times(1)).matches(rawPassword, user.getPassword());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 유저")
    void signInUserFailedInvalidUsername() {
        // given
        UserSignInRequestDto signInRequest =
                createSignInRequest("wrongUsername", "password");

        when(userRepository.findByUsername(signInRequest.username()))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.signIn(signInRequest));

        assertEquals(INVALID_USERNAME_OR_PASSWORD, exception.getErrorCode());
        verify(userRepository, times(1)).findByUsername(signInRequest.username());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 패스워드 불일치")
    void signInUserFailedInvalidPassword() {
        // given
        String rawPassword = "wrongPassword";

        User user = User.builder()
                .username("existsUsername")
                .nickname("test")
                .email("test@email.com")
                .password("encodedPassword")
                .role(USER)
                .build();

        UserSignInRequestDto signInRequest =
                createSignInRequest(user.getUsername(), rawPassword);

        when(userRepository.findByUsername(signInRequest.username()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, user.getPassword()))
                .thenReturn(false);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.signIn(signInRequest));

        assertEquals(INVALID_USERNAME_OR_PASSWORD, exception.getErrorCode());
        verify(userRepository, times(1)).findByUsername(signInRequest.username());
        verify(passwordEncoder, times(1)).matches(rawPassword, user.getPassword());
    }

    @Test
    @DisplayName("회원가입 시 Blog 자동 생성")
    void signUpCreatesBlog() {
        // given
        UserSignUpRequestDto requestDto = createTestRequest();

        when(userRepository.existsByUsername(requestDto.username())).thenReturn(false);
        when(userRepository.existsByNickname(requestDto.nickname())).thenReturn(false);
        when(userRepository.existsByEmail(requestDto.email())).thenReturn(false);
        when(passwordEncoder.encode(requestDto.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));


        // when
        userService.signUp(requestDto);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser.getBlog());
        assertEquals(savedUser, savedUser.getBlog().getUser());
        assertEquals("", savedUser.getBlog().getIntro());
        assertEquals(savedUser.getNickname() + "의 블로그", savedUser.getBlog().getTitle());
    }

    @Test
    @DisplayName("username 수정 성공")
    void updateUsernameSuccess() {
        // given
        User user = User.builder().userId(1L).username("oldUsername").build();

        UpdateUsernameRequestDto requestDto = new UpdateUsernameRequestDto("newUsername");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(requestDto.username())).thenReturn(false);

        // when
        User updatedUser = userService.updateUsername(user.getUserId(), requestDto);

        // then
        assertNotNull(updatedUser);
        assertEquals("newUsername", updatedUser.getUsername());
    }

    @Test
    @DisplayName("username 수정 실패 - 이미 존재하는 username")
    void updateUsernameFailedExistsUsername() {
        // given
        User user = User.builder().userId(1L).username("oldUsername").build();
        UpdateUsernameRequestDto requestDto = new UpdateUsernameRequestDto("existUsername");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(requestDto.username())).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.updateUsername(user.getUserId(), requestDto));

        assertEquals(DUPLICATED_USERNAME, exception.getErrorCode());
    }

    @Test
    @DisplayName("username 수정 실패 - 이전과 같은 닉네임")
    void updateUsernameFailedNotNewUsername() {
        // given
        User user = User.builder().userId(1L).username("oldUsername").build();
        UpdateUsernameRequestDto requestDto = new UpdateUsernameRequestDto("oldUsername");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.updateUsername(user.getUserId(), requestDto));

        assertEquals(SAME_USERNAME, exception.getErrorCode());
    }

    @Test
    @DisplayName("비밀번호 수정 성공")
    void updateUserPasswordSuccess() {
        // given
        User user = User.builder().userId(1L).password("encodedOldPassword").build();
        String encodedNewPassword = "encodedNewPassword";
        UpdateUserPasswordRequestDto requestDto =
                new UpdateUserPasswordRequestDto("oldPassword", "newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(requestDto.oldPassword(), user.getPassword()))
                .thenReturn(true);
        when(passwordEncoder.encode(requestDto.rawPassword()))
                .thenReturn(encodedNewPassword);

        // when
        User updatedUser = userService.updateUserPassword(user.getUserId(), requestDto);

        // then
        assertNotNull(updatedUser);
        assertEquals(encodedNewPassword, updatedUser.getPassword());
    }

    @Test
    @DisplayName("비밀번호 수정 실패 - 비밀번호가 일치하지 않음")
    void updateUserPasswordFailedInvalidOldPassword() {
        // given
        User user = User.builder().userId(1L).password("encodedOldPassword").build();
        UpdateUserPasswordRequestDto requestDto =
                new UpdateUserPasswordRequestDto("wrongPassword", "newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(requestDto.oldPassword(), user.getPassword()))
                .thenReturn(false);
        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.updateUserPassword(user.getUserId(), requestDto));

        assertEquals(INVALID_OLD_PASSWORD, exception.getErrorCode());
    }

    @Test
    @DisplayName("닉네임 수정 성공")
    void UpdateUserNicknameSuccess() {
        // given
        User user = User.builder().userId(1L).nickname("oldNickname").build();

        UpdateUserNicknameRequestDto requestDto =
                new UpdateUserNicknameRequestDto("newNickname");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname(requestDto.nickname()))
                .thenReturn(false);

        // when
        User updatedUser = userService.updateUserNickname(user.getUserId(), requestDto);

        // then
        assertNotNull(updatedUser);
        assertEquals("newNickname", updatedUser.getNickname());
    }

    @Test
    @DisplayName("닉네임 수정 실패 - 이전과 동일한 닉네임")
    void UpdateUserNicknameFailedSameNickname() {
        // given
        User user = User.builder().userId(1L).nickname("oldNickname").build();

        UpdateUserNicknameRequestDto requestDto =
                new UpdateUserNicknameRequestDto("oldNickname");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.updateUserNickname(user.getUserId(), requestDto));
        assertEquals(SAME_NICKNAME, exception.getErrorCode());
    }

    @Test
    @DisplayName("닉네임 수정 실패 - 닉네임 중복")
    void UpdateUserNicknameFailedDuplicatedNickname() {
        // given
        User user = User.builder().userId(1L).nickname("oldNickname").build();

        UpdateUserNicknameRequestDto requestDto =
                new UpdateUserNicknameRequestDto("existsNickname");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname(requestDto.nickname()))
                .thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.updateUserNickname(user.getUserId(), requestDto));
        assertEquals(DUPLICATED_NICKNAME, exception.getErrorCode());
    }

    @Test
    @DisplayName("이메일 수정 성공")
    void UpdateUserEmailSuccess() {
        // given
        User user = User.builder().userId(1L).email("oldEmail").build();

        UpdateUserEmailRequestDto requestDto =
                new UpdateUserEmailRequestDto("newEmail");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(requestDto.email()))
                .thenReturn(false);

        // when
        User updatedUser = userService.updateUserEmail(user.getUserId(), requestDto);

        // then
        assertNotNull(updatedUser);
        assertEquals("newEmail", updatedUser.getEmail());
    }

    @Test
    @DisplayName("이메일 수정 실패 - 이전과 동일한 이메일")
    void UpdateUserEmailFailedSameEmail() {
        // given
        User user = User.builder().userId(1L).email("oldEmail").build();

        UpdateUserEmailRequestDto requestDto =
                new UpdateUserEmailRequestDto("oldEmail");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.updateUserEmail(user.getUserId(), requestDto));
        assertEquals(SAME_EMAIL, exception.getErrorCode());
    }

    @Test
    @DisplayName("이메일 수정 실패 - 이메일 중복")
    void UpdateUserEmailFailedDuplicatedEmail() {
        // given
        User user = User.builder().userId(1L).email("oldEmail").build();

        UpdateUserEmailRequestDto requestDto =
                new UpdateUserEmailRequestDto("existsEmail");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(requestDto.email()))
                .thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.updateUserEmail(user.getUserId(), requestDto));
        assertEquals(DUPLICATED_EMAIL, exception.getErrorCode());
    }

    @Test
    @DisplayName("프로필 이미지 수정 성공 - 기존 이미지 없음")
    void UpdateUserProfileImageSuccessNoOldImage() {
        // given
        MultipartFile file = mock(MultipartFile.class);

        User user = User.builder().userId(1L).profileImageUrl(null).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(minioService.upload(user.getUserId(), file, PROFILE_IMAGE))
                .thenReturn("https://minio/new.png");

        // when
        User updatedUser = userService.updateUserProfileImage(user.getUserId(), file);

        // then
        assertNotNull(updatedUser);
        assertEquals("https://minio/new.png", updatedUser.getProfileImageUrl());

        verify(minioService).upload(user.getUserId(), file, PROFILE_IMAGE);
        verify(minioService, never()).extractObjectNameFromUrl(anyString());
        verify(minioService, never()).deleteObject(anyString());
    }

    @Test
    @DisplayName("프로필 이미지 수정 성공 - 기존 이미지 삭제")
    void UpdateUserProfileSuccessWithOldImage() {
        // given
        MultipartFile file = mock(MultipartFile.class);

        String oldImageUrl = "https://minio/bucket/users/1/profile/old.png";
        String oldObjectName = "users/1/profile/old.png";

        User user = User.builder()
                .userId(1L).profileImageUrl(oldImageUrl)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(minioService.upload(user.getUserId(), file, PROFILE_IMAGE))
                .thenReturn("https://minio/new.png");
        when(minioService.extractObjectNameFromUrl(oldImageUrl))
                .thenReturn(oldObjectName);

        // when
        User updatedUser = userService.updateUserProfileImage(user.getUserId(), file);

        // then
        assertEquals("https://minio/new.png", updatedUser.getProfileImageUrl());

        verify(minioService).upload(user.getUserId(), file, PROFILE_IMAGE);
        verify(minioService).extractObjectNameFromUrl(oldImageUrl);
        verify(minioService).deleteObject(oldObjectName);
    }

    @Test
    @DisplayName("프로필 이미지 삭제 성공")
    void DeleteUserProfileImageSuccess() {
        // given
        String oldImageUrl = "https://minio/bucket/users/1/profile/old.png";
        String oldObjectName = "users/1/profile/old.png";

        User user = User.builder()
                .userId(1L).profileImageUrl(oldImageUrl)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(minioService.extractObjectNameFromUrl(oldImageUrl))
                .thenReturn(oldObjectName);

        // when
        User updatedUser = userService.deleteUserProfileImage(user.getUserId());

        // then
        assertNotNull(updatedUser);
        assertNull(updatedUser.getProfileImageUrl());

        verify(minioService).extractObjectNameFromUrl(oldImageUrl);
        verify(minioService).deleteObject(oldObjectName);
    }

    private UserSignUpRequestDto createTestRequest() {
        return new UserSignUpRequestDto("newUser", "password", "newNickname", "new@email.com");
    }

    private UserSignInRequestDto createSignInRequest(String username, String password) {
        return new UserSignInRequestDto(username, password);
    }

}
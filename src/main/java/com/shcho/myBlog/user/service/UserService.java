package com.shcho.myBlog.user.service;

import com.shcho.myBlog.blog.entity.Blog;
import com.shcho.myBlog.category.entity.Category;
import com.shcho.myBlog.category.repository.CategoryRepository;
import com.shcho.myBlog.common.service.MinioService;
import com.shcho.myBlog.common.util.JwtProvider;
import com.shcho.myBlog.libs.exception.CustomException;
import com.shcho.myBlog.user.dto.*;
import com.shcho.myBlog.user.entity.User;
import com.shcho.myBlog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.shcho.myBlog.common.entity.UploadType.PROFILE_IMAGE;
import static com.shcho.myBlog.libs.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final String DEFAULT_CATEGORY_NAME = "미분류";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CategoryRepository categoryRepository;
    private final MinioService minioService;

    @Transactional
    public User signUp(UserSignUpRequestDto requestDto) {

        String username = requestDto.username().trim();
        String password = requestDto.password();
        String nickname = requestDto.nickname();
        String email = requestDto.email();

        if (userRepository.existsByUsername(username)) {
            throw new CustomException(DUPLICATED_USERNAME);
        }

        if (userRepository.existsByEmail(email)) {
            throw new CustomException(DUPLICATED_EMAIL);
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(DUPLICATED_NICKNAME);
        }

        String encodedPassword = passwordEncoder.encode(password);

        User signUpUser = User.of(
                username,
                encodedPassword,
                nickname,
                email
        );

        signUpUser.setBlog(Blog.ofDefault(signUpUser));

        User savedUser = userRepository.save(signUpUser);

        Blog savedBlog = savedUser.getBlog();
        Category defaultCategory = Category.of(savedBlog, null, DEFAULT_CATEGORY_NAME, "");
        categoryRepository.save(defaultCategory);

        return savedUser;
    }

    public User signIn(UserSignInRequestDto requestDto) {
        String username = requestDto.username();
        String password = requestDto.password();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(INVALID_USERNAME_OR_PASSWORD));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(INVALID_USERNAME_OR_PASSWORD);
        }

        return user;
    }

    @Transactional
    public User updateUsername(Long userId, UpdateUsernameRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String newUsername = requestDto.username().trim();

        if (!newUsername.equals(user.getUsername()) &&
                userRepository.existsByUsername(newUsername)) {
            throw new CustomException(DUPLICATED_USERNAME);
        }

        user.updateUsername(newUsername);

        return user;
    }

    @Transactional
    public User updateUserPassword(Long userId, UpdateUserPasswordRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String oldPassword = requestDto.oldPassword();
        String newPassword = requestDto.rawPassword();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new CustomException(INVALID_OLD_PASSWORD);
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        user.updatePassword(encodedPassword);

        return user;
    }

    @Transactional
    public User updateUserNickname(Long userId, UpdateUserNicknameRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String newNickname = requestDto.nickname().trim();

        if (!newNickname.equals(user.getNickname()) &&
                userRepository.existsByNickname(newNickname)) {
            throw new CustomException(DUPLICATED_NICKNAME);
        }

        user.updateNickname(newNickname);

        return user;
    }

    @Transactional
    public User updateUserEmail(Long userId, UpdateUserEmailRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String newEmail = requestDto.email().trim();

        if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new CustomException(DUPLICATED_EMAIL);
        }

        user.updateEmail(newEmail);

        return user;
    }

    @Transactional
    public User updateUserProfileImage(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String oldUrl = user.getProfileImageUrl();

        String url = minioService.upload(userId, file, PROFILE_IMAGE);

        user.updateProfileImageUrl(url);

        if (oldUrl != null && !oldUrl.isBlank()) {
            String oldObjectName = minioService.extractObjectNameFromUrl(oldUrl);

            if (oldObjectName != null && !oldObjectName.isBlank()) {
                try {
                    minioService.deleteObject(oldObjectName);
                } catch (Exception e) {
                    log.warn("프로필 이미지 교체 중 이전 파일 삭제 실패. userId={}, oldObjectName={}",
                            userId, oldObjectName, e);
                }
            }
        }

        return user;
    }

    @Transactional
    public User deleteUserProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String oldUrl = user.getProfileImageUrl();
        if (oldUrl != null && !oldUrl.isBlank()) {
            String oldObjectName = minioService.extractObjectNameFromUrl(oldUrl);
            if (oldObjectName != null && !oldObjectName.isBlank()) {
                minioService.deleteObject(oldObjectName);
            }
        }

        user.deleteProfileImageUrl();

        return user;
    }

    public String getUserToken(User user) {
        return jwtProvider.createToken(user.getUsername(), user.getRole());
    }

    public long getExpiresInSeconds(String token) {
        return jwtProvider.getExpirationInSeconds(token);
    }
}

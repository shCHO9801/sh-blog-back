package com.shcho.myBlog.user.service;

import com.shcho.myBlog.blog.entity.Blog;
import com.shcho.myBlog.common.util.JwtProvider;
import com.shcho.myBlog.libs.exception.CustomException;
import com.shcho.myBlog.user.dto.UserSignInRequestDto;
import com.shcho.myBlog.user.dto.UserSignUpRequestDto;
import com.shcho.myBlog.user.entity.User;
import com.shcho.myBlog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.shcho.myBlog.libs.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public User signUp(UserSignUpRequestDto requestDto) {

        String username = requestDto.username();
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

        return userRepository.save(signUpUser);
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

    public String getUserToken(User user) {
        return jwtProvider.createToken(user.getUsername(), user.getRole());
    }
}

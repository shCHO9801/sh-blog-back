package com.shcho.myBlog.user.controller;

import com.shcho.myBlog.user.dto.UserSignInRequestDto;
import com.shcho.myBlog.user.dto.UserSignInResponseDto;
import com.shcho.myBlog.user.dto.UserSignUpRequestDto;
import com.shcho.myBlog.user.dto.UserSignUpResponseDto;
import com.shcho.myBlog.user.entity.User;
import com.shcho.myBlog.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserSignUpResponseDto> signUp(
            @Valid @RequestBody UserSignUpRequestDto requestDto
    ) {
        User signUpUser = userService.signUp(requestDto);
        return ResponseEntity.ok(UserSignUpResponseDto.from(signUpUser));
    }

    @PostMapping("/signin")
    public ResponseEntity<UserSignInResponseDto> signIn(
            @Valid @RequestBody UserSignInRequestDto requestDto
    ) {
        User user = userService.signIn(requestDto);
        String token = userService.getUserToken(user);

        return ResponseEntity.ok(UserSignInResponseDto.of(user, token));
    }
}

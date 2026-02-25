package com.shcho.myBlog.user.controller;

import com.shcho.myBlog.user.auth.CustomUserDetails;
import com.shcho.myBlog.user.dto.*;
import com.shcho.myBlog.user.entity.User;
import com.shcho.myBlog.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserMeResponseDto> me(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(UserMeResponseDto.from(user));
    }

    @PatchMapping("/me/username")
    public ResponseEntity<UserMeResponseDto> updateUsername(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateUsernameRequestDto requestDto
    ) {
        User user = userService.updateUsername(userDetails.getUserId(), requestDto);
        return ResponseEntity.ok(UserMeResponseDto.from(user));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<UserMeResponseDto> updateUserPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateUserPasswordRequestDto requestDto
    ) {
        User user = userService.updateUserPassword(userDetails.getUserId(), requestDto);
        return ResponseEntity.ok(UserMeResponseDto.from(user));
    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<UserMeResponseDto> updateUserNickname(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateUserNicknameRequestDto requestDto
    ) {
        User user = userService.updateUserNickname(userDetails.getUserId(), requestDto);
        return ResponseEntity.ok(UserMeResponseDto.from(user));
    }

    @PatchMapping("/me/email")
    public ResponseEntity<UserMeResponseDto> updateUserEmail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateUserEmailRequestDto requestDto
    ) {
        User user = userService.updateUserEmail(userDetails.getUserId(), requestDto);
        return ResponseEntity.ok(UserMeResponseDto.from(user));
    }

    @PutMapping(value = "/me/profile-image", consumes = "multipart/form-data")
    public ResponseEntity<UserMeResponseDto> updateUserProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) {
        User user = userService.updateUserProfileImage(userDetails.getUserId(), file);

        return ResponseEntity.ok(UserMeResponseDto.from(user));
    }

    @DeleteMapping("/me/profile-image")
    public ResponseEntity<UserMeResponseDto> deleteUserProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userService.deleteUserProfileImage(userDetails.getUserId());
        return ResponseEntity.ok(UserMeResponseDto.from(user));
    }
}

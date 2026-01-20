package com.shcho.myBlog.user.controller;

import com.shcho.myBlog.user.auth.CustomUserDetails;
import com.shcho.myBlog.user.dto.UserMeResponseDto;
import com.shcho.myBlog.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserMeResponseDto> me(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(UserMeResponseDto.from(user));
    }
}

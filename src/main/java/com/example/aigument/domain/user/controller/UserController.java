package com.example.aigument.domain.user.controller;

import com.example.aigument.common.dto.response.CommonResponse;
import com.example.aigument.domain.user.dto.response.GetUserResponse;
import com.example.aigument.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<GetUserResponse>> getUser(@PathVariable Long id) {

        GetUserResponse response = userService.getUser(id);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success("사용자 조회에 성공했습니다.", response));
    }
}

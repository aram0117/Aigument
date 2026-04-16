package com.example.aigument.domain.user.controller;

import com.example.aigument.common.dto.response.CommonResponse;
import com.example.aigument.domain.auth.dto.AuthUser;
import com.example.aigument.domain.user.dto.request.UpdateUserRequest;
import com.example.aigument.domain.user.dto.response.GetUserResponse;
import com.example.aigument.domain.user.dto.response.UpdateUserResponse;
import com.example.aigument.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "사용자 본인의 계정 정보를 조회합니다.")
    @GetMapping()
    public ResponseEntity<CommonResponse<GetUserResponse>> getUser(@AuthenticationPrincipal AuthUser authUser) {

        GetUserResponse response = userService.getUser(authUser);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success("사용자 조회에 성공했습니다.", response));
    }

    @Operation(summary = "내 정보 부분 수정", description = "사용자 본인의 닉네임과 이메일을 부분 수정합니다.")
    @PatchMapping()
    public ResponseEntity<CommonResponse<UpdateUserResponse>> updateUser(@AuthenticationPrincipal AuthUser authUser, @RequestBody UpdateUserRequest request) {

        UpdateUserResponse response = userService.updateUser(authUser, request);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success("사용자 정보 수정에 성공했습니다.", response));
    }
}

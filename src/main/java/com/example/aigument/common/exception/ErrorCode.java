package com.example.aigument.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // user
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    SOCIAL_ACCOUNT_CANNOT_USER_MODIFY(HttpStatus.FORBIDDEN, "소셜 로그인 계정은 사용자 정보를 수정할 수 없습니다."),
    WRONG_PASSWORD_CONFIRM(HttpStatus.BAD_REQUEST, "입력하신 비밀번호를 다시 확인해 주세요."),
    NOT_FOUND_USER_STATS(HttpStatus.NOT_FOUND, "사용자 상태를 찾을 수 없습니다."),

    // auth
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "중복된 닉네임 입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "중복된 이메일 입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "인증 정보가 없거나 유효하지 않습니다."),
    UNSUPPORTED_SOCIAL_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 공급자입니다."),
    LOGGED_OUT_TOKEN(HttpStatus.UNAUTHORIZED, "로그아웃된 토큰입니다. 다시 로그인해주세요."),

    // chatroom
    NOT_FOUND_CHATROOM(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    CHATROOM_ALREADY_FULL(HttpStatus.CONFLICT, "이미 게스트가 존재하여 입장할 수 없는 채팅방입니다."),
    ONLY_ONE_CHATROOM_ALLOWED(HttpStatus.FORBIDDEN,"사용자는 하나의 채팅방만 접근할 수 있습니다."),
    NOT_FOUND_USER_IN_CHATROOM(HttpStatus.FORBIDDEN, "채팅방 내에서 사용자를 찾을 수 없습니다."),

    // ai
    AI_COMMUNICATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI 서버와 통신하는 중 문제가 발생했습니다."),
    AI_RESPONSE_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "AI 분석 시간이 너무 오래 걸려 요청이 취소되었습니다."),
    EMPTY_CHAT_LOG(HttpStatus.BAD_REQUEST, "채팅 기록이 비어 있어 분석을 진행할 수 없습니다."),
    AI_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 토론 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
    ;

    private final HttpStatus status;
    private final String message;
}

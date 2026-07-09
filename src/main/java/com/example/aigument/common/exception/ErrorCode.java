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
    UNSUPPORTED_SOCIAL_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 공급자입니다."),
    LOGGED_OUT_TOKEN(HttpStatus.UNAUTHORIZED, "로그아웃된 토큰입니다. 다시 로그인해주세요."),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"인증번호 발송 중 에러가 발생했습니다."),
    AUTH_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    INVALID_REGISTRATION_STEP(HttpStatus.FORBIDDEN, "인증 번호가 발급된 휴대폰 번호로 회원가입을 진행해주세요."),
    UNVERIFIED_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "가입된 휴대폰 번호로 인증코드를 발급해주세요."),

    // chatroom
    NOT_FOUND_CHATROOM(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    CHATROOM_ALREADY_FULL(HttpStatus.CONFLICT, "이미 게스트가 존재하여 입장할 수 없는 채팅방입니다."),
    ONLY_ONE_CHATROOM_ALLOWED(HttpStatus.FORBIDDEN,"사용자는 하나의 채팅방만 접근할 수 있습니다."),
    NOT_FOUND_USER_IN_CHATROOM(HttpStatus.FORBIDDEN, "채팅방 내에서 사용자를 찾을 수 없습니다."),

    // ai
    EMPTY_CHAT_LOG(HttpStatus.BAD_REQUEST, "채팅 기록이 비어 있어 분석을 진행할 수 없습니다."),
    AI_HALLUCINATED_PARTICIPANT(HttpStatus.INTERNAL_SERVER_ERROR, "AI 분석 결과에 현재 채팅방에 존재하지 않는 사용자가 포함되어 있습니다."),


    // stomp
    STOMP_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "스톰프 인증 토큰이 유효하지 않습니다."),
    STOMP_MISSING_AUTH(HttpStatus.UNAUTHORIZED, "스톰프 인증 정보가 존재하지 않습니다.")


    ;

    private final HttpStatus status;
    private final String message;
}

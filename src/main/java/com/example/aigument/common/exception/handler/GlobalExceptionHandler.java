package com.example.aigument.common.exception.handler;

import com.example.aigument.common.dto.response.CommonResponse;
import com.example.aigument.common.exception.CustomException;
import com.example.aigument.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CommonResponse<Void>> customException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();

        // 정적 메서드 success/error를 제네릭하게 호출
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(CommonResponse.error(errorCode.getMessage()));
    }

    // @Valid 검증 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 첫 번째 에러 메시지를 가져오되, NPE 방지를 위해 Objects.requireNonNull 사용 권장
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.error(message));
    }

    // 예상하지 못한 예외 처리 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        log.error("예상하지 못한 예외 발생: ", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error("서버 내부 오류가 발생했습니다."));
    }

    // 정적 리소스를 찾지 못할 때 (404) - 데이터 없이 상태코드만 반환하는 경우
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFoundException() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
package com.hyunsu.springboot.global.exception;

import com.hyunsu.springboot.global.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;

/**
 * 전역 예외 처리.
 * 모든 Controller에서 발생하는 예외를 여기서 잡아 ApiResponse.fail() 포맷으로 통일해 응답한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 도메인에서 명시적으로 던진 커스텀 예외 처리 (가장 우선순위 높은 케이스)
     * 예: MemberNotFoundException, UsernameDuplicatedException 등
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("CustomException: code={}, message={}", errorCode.getCode(), e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getStatus(), errorCode.getCode(), e.getMessage()));
    }

    /**
     * @Valid 검증 실패 시 (DTO 필드 검증)
     * 여러 필드가 동시에 실패할 수 있어, 첫 번째 에러 메시지를 대표로 사용
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = (fieldError != null)
                ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
                : ErrorCode.INVALID_INPUT_VALUE.getMessage();

        log.warn("Validation failed: {}", message);

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE.getStatus(), ErrorCode.INVALID_INPUT_VALUE.getCode(), message));
    }

    /**
     * 위에서 처리되지 않은 모든 예외 (예상치 못한 서버 오류)
     * 클라이언트에는 상세 내용을 숨기고, 서버 로그에만 스택트레이스를 남긴다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception occurred", e);

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.fail(
                        ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                ));
    }

    /**
     * 매핑된 컨트롤러가 없는 경로 접근 처리.
     * /api/**  → 404 JSON 응답
     * 그 외     → 404 화면으로 리다이렉트
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException e,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String uri = request.getRequestURI();

        if (uri.startsWith("/api/")) {
            log.warn("존재하지 않는 API 경로: {}", uri);
            ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        // 화면 경로면 404 페이지로 직접 리다이렉트
        response.sendRedirect("/error/404");
        return null;
    }
}
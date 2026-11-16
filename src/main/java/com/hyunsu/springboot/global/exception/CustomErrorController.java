package com.hyunsu.springboot.global.exception;

import com.hyunsu.springboot.global.common.response.ApiResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring Boot 기본 에러 처리(/error)를 가로채서, 요청 경로에 따라 응답 형식을 분기한다.
 *
 * - "/api/**" 요청 중 발생한 에러 (404 포함) -> ApiResponse.fail() JSON 응답
 * - 그 외(화면) 요청 중 발생한 에러            -> 404.html 등 정적 에러 페이지로 forward
 *
 * 404는 핸들러 자체를 못 찾은 상태라 GlobalExceptionHandler(@RestControllerAdvice)로는
 * 잡을 수 없어서, ErrorController를 직접 구현해 처리한다.
 *
 * 주의: 우리가 만드는 API는 전부 "/api"로 시작하는 것을 규칙으로 한다.
 *       (member, auth, board 모든 컨트롤러의 @RequestMapping 베이스 경로에 /api 사용)
 */
@RestController
public class CustomErrorController implements ErrorController {

    private static final String ERROR_PATH = "/error";
    private static final String API_PATH_PREFIX = "/api";

    // /error는 원래 요청이 GET이든 POST/PUT/DELETE든 그 요청에서 난 에러를 모두 받아야 한다.
    // 특정 메서드만 지정하면 그 외 메서드 요청의 에러가 405로 바뀌어버리므로, 모든 메서드를 명시적으로 허용한다.
    @RequestMapping(value = ERROR_PATH, method = {
            RequestMethod.GET, RequestMethod.HEAD, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS
    })
    public Object handleError(HttpServletRequest request) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = (statusAttr != null)
                ? HttpStatus.valueOf(Integer.parseInt(statusAttr.toString()))
                : HttpStatus.INTERNAL_SERVER_ERROR;

        Object originalUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String requestPath = (originalUri != null) ? originalUri.toString() : "";

        if (requestPath.startsWith(API_PATH_PREFIX)) {
            return handleApiError(status);
        }
        return handlePageError(status);
    }

    /**
     * /api/** 요청 중 에러 -> JSON 응답
     */
    private ResponseEntity<ApiResponse<Void>> handleApiError(HttpStatus status) {
        ErrorCode errorCode = (status == HttpStatus.NOT_FOUND)
                ? ErrorCode.API_NOT_FOUND
                : ErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(status)
                .body(ApiResponse.fail(status, errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * 화면 요청 중 에러 -> 정적 에러 페이지로 forward
     * static/404.html, static/500.html 등을 미리 만들어두고 사용
     */
    private ModelAndView handlePageError(HttpStatus status) {
        String viewName = (status == HttpStatus.NOT_FOUND) ? "forward:/404.html" : "forward:/500.html";
        return new ModelAndView(viewName);
    }
}
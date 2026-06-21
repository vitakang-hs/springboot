package com.hyunsu.springboot.global.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 정적 HTML(static)을 깔끔한 URL로 매핑해주는 View Controller.
 * 실제 데이터 처리는 하지 않고 forward만 담당한다.
 *
 * Spring Security 적용 시, 아래 경로들은 permitAll() 처리 필요:
 *   "/", "/signup", "/oauth/success", "/main", "/css/**", "/js/**"
 */
@Controller
public class ViewController {

    @GetMapping("/signup")
    public String signup() {
        return "forward:/signup.html";
    }

    /**
     * OAuth2 로그인 성공 후 리다이렉트되는 경로.
     */
    @GetMapping("/oauth/success")
    public String oauthSuccess() {
        return "forward:/oauth-success.html";
    }

    /**
     * 로그인 후 진입하는 메인 페이지.
     * 인증 여부 자체는 SecurityConfig에서 permitAll()이지만,
     * main.js가 Access Token 없으면(재발급도 실패하면) 로그인 페이지로 다시 보낸다.
     */
    @GetMapping("/main")
    public String main() {
        return "forward:/main.html";
    }
}
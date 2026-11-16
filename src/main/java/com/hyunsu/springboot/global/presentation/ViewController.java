package com.hyunsu.springboot.global.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 정적 HTML(static)을 깔끔한 URL로 매핑해주는 View Controller.
 *
 * 정적 파일 폴더 구조:
 *   static/index.html              → Spring Boot Welcome Page로 자동 서빙
 *   static/error/404.html
 *   static/auth/signup.html
 *   static/auth/oauth-success.html
 *   static/admin/main.html
 *   static/admin/board/list.html
 *   static/admin/board/detail.html
 *   static/admin/board/write.html   (작성/수정 공용)
 */
@Controller
public class ViewController {

    @GetMapping("/signup")
    public String signup() {
        return "forward:/html/auth/signup.html";
    }

    @GetMapping("/oauth/success")
    public String oauthSuccess() {
        return "forward:/html/auth/oauth-success.html";
    }

    @GetMapping("/main")
    public String main() {
        return "forward:/html/admin/main.html";
    }

    /**
     * 게시판 목록.
     * 인증 여부는 board-list.js가 tryReissue()로 확인한다.
     */
    @GetMapping("/board")
    public String boardList() {
        return "forward:/html/admin/board/list.html";
    }

    /**
     * 게시글 작성. 수정(/board/{id}/edit)과 같은 화면을 쓰며, write.js가 URL로 모드를 구분한다.
     */
    @GetMapping("/board/write")
    public String boardWrite() {
        return "forward:/html/admin/board/write.html";
    }

    /**
     * 게시글 상세. {postId}는 숫자만 매칭해 /board/write 같은 경로와 겹치지 않게 한다.
     */
    @GetMapping("/board/{postId:\\d+}")
    public String boardDetail() {
        return "forward:/html/admin/board/detail.html";
    }

    @GetMapping("/board/{postId:\\d+}/edit")
    public String boardEdit() {
        return "forward:/html/admin/board/write.html";
    }

    @GetMapping("/error/404")
    public String error404() {
        return "forward:/html/error/404.html";
    }
}
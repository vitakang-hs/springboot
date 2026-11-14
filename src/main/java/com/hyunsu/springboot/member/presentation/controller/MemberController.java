package com.hyunsu.springboot.member.presentation.controller;

import com.hyunsu.springboot.global.common.response.ApiResponse;
import com.hyunsu.springboot.member.application.command.MemberEmailCommandService;
import com.hyunsu.springboot.member.application.command.MemberRegisterCommandService;
import com.hyunsu.springboot.member.application.command.dto.EmailCodeVerifyCommand;
import com.hyunsu.springboot.member.application.command.dto.EmailCodeVerifyCommandResult;
import com.hyunsu.springboot.member.application.command.dto.MemberRegisterCommand;
import com.hyunsu.springboot.member.application.query.MemberAvailabilityQueryService;
import com.hyunsu.springboot.member.application.query.MemberMeQueryService;
import com.hyunsu.springboot.member.application.query.VerifiedEmailQueryService;
import com.hyunsu.springboot.member.presentation.dto.request.EmailCodeVerifyRequest;
import com.hyunsu.springboot.member.presentation.dto.request.EmailVerificationTokenRequest;
import com.hyunsu.springboot.member.presentation.dto.request.MemberSignUpRequest;
import com.hyunsu.springboot.member.presentation.dto.response.EmailCodeSendResponse;
import com.hyunsu.springboot.member.presentation.dto.response.EmailCodeVerifyResponse;
import com.hyunsu.springboot.member.presentation.dto.response.MemberMeResponse;
import com.hyunsu.springboot.member.presentation.dto.response.UsernameCheckResponse;
import com.hyunsu.springboot.member.presentation.dto.response.VerifiedEmailResponse;
import com.hyunsu.springboot.member.presentation.mapper.MemberMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원(Member) 관련 API.
 * 모든 경로는 "/api"로 시작한다.
 *
 * SecurityConfig에서 /api/members/signup, /check-username, /email/** 은
 * permitAll() 처리되어 있음. 그 외(/me 등)는 인증 필요.
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
public class MemberController {

    private final MemberAvailabilityQueryService memberAvailabilityQueryService;
    private final MemberMeQueryService memberMeQueryService;
    private final MemberEmailCommandService memberEmailCommandService;
    private final MemberRegisterCommandService memberRegisterCommandService;
    private final VerifiedEmailQueryService verifiedEmailQueryService;
    private final MemberMapper memberMapper;

    /**
     * 아이디 중복확인.
     * GET /api/members/check-username?username=vitakang
     */
    @GetMapping("/check-username")
    public ApiResponse<UsernameCheckResponse> checkUsername(@RequestParam String username) {
        boolean isDuplicated = memberAvailabilityQueryService.isUsernameDuplicated(username);
        boolean isAvailable = !isDuplicated;

        return ApiResponse.success(new UsernameCheckResponse(isAvailable));
    }

    /**
     * 로그인한 본인 정보 조회.
     * GET /api/members/me
     *
     * JwtAuthenticationFilter가 SecurityContext에 채워둔 memberId를 @AuthenticationPrincipal로 꺼낸다.
     */
    @GetMapping("/me")
    public ApiResponse<MemberMeResponse> getMe(@AuthenticationPrincipal Long memberId) {
        MemberMeResponse response = memberMeQueryService.getMe(memberId);
        return ApiResponse.success(response);
    }

    /**
     * 이메일 인증코드 발송.
     * POST /api/members/email/send-code?email=test@example.com
     */
    @PostMapping("/email/send-code")
    public ApiResponse<EmailCodeSendResponse> sendEmailCode(
            @RequestParam @NotBlank @Email String email
    ) {
        String code = memberEmailCommandService.sendVerificationCode(email);
        return ApiResponse.success(new EmailCodeSendResponse(code));
    }

    /**
     * 이메일 인증코드 검증.
     * POST /api/members/email/verify-code
     *
     * 성공 시 인증 완료 증명 토큰(verificationToken)을 함께 내려준다. 회원가입 요청 시 이 토큰을 보내야 한다.
     */
    @PostMapping("/email/verify-code")
    public ApiResponse<EmailCodeVerifyResponse> verifyEmailCode(@RequestBody @Valid EmailCodeVerifyRequest request) {
        EmailCodeVerifyCommand command = memberMapper.toCommand(request);
        EmailCodeVerifyCommandResult result = memberEmailCommandService.verifyCode(command);
        return ApiResponse.success(memberMapper.toResponse(result));
    }

    /**
     * 인증 완료 증명 토큰으로 인증된 이메일 조회.
     * POST /api/members/email/verified
     *
     * 구글 신규 가입자가 회원가입 페이지(?token=)에 진입했을 때 이메일을 폼에 채우기 위해 사용한다.
     */
    @PostMapping("/email/verified")
    public ApiResponse<VerifiedEmailResponse> getVerifiedEmail(@RequestBody @Valid EmailVerificationTokenRequest request) {
        String email = verifiedEmailQueryService.getVerifiedEmail(request.token());
        return ApiResponse.success(new VerifiedEmailResponse(email));
    }

    /**
     * 회원가입.
     * POST /api/members/signup
     */
    @PostMapping("/signup")
    public ApiResponse<Void> signUp(@RequestBody @Valid MemberSignUpRequest request) {
        MemberRegisterCommand command = memberMapper.toCommand(request);
        memberRegisterCommandService.register(command);
        return ApiResponse.success(HttpStatus.CREATED);
    }
}
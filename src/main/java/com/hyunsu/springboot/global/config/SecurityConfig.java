package com.hyunsu.springboot.global.config;

import com.hyunsu.springboot.global.security.AuthenticationTokenFilterProvider;
import com.hyunsu.springboot.global.security.JwtAuthenticationEntryPoint;
import com.hyunsu.springboot.global.security.OAuthLoginProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationTokenFilterProvider authenticationTokenFilterProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final OAuthLoginProvider oAuthLoginProvider;

    public SecurityConfig(
            AuthenticationTokenFilterProvider authenticationTokenFilterProvider,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            OAuthLoginProvider oAuthLoginProvider
    ) {
        this.authenticationTokenFilterProvider = authenticationTokenFilterProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.oAuthLoginProvider = oAuthLoginProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CSRF 보호 비활성화 - 검토 완료, 안전함:
                //  - API 인증은 쿠키가 아닌 Authorization: Bearer 헤더(JS가 직접 첨부)로 한다.
                //    브라우저가 다른 사이트의 요청에 이 헤더를 자동으로 붙이지 않으므로 CSRF가 성립하지 않는다.
                //  - 쿠키를 쓰는 유일한 API(/api/auth/reissue)의 Refresh Token 쿠키는 SameSite=Strict라
                //    다른 사이트에서 시작된 요청에는 쿠키가 실리지 않는다.
                //  - 세션을 쓰지 않는다 (STATELESS).
                .csrf(AbstractHttpConfigurer::disable) // NOSONAR - 위 사유로 검토 완료 (java:S4502)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth
                        // 화면 (정적 리소스 + 정적 HTML)
                        .requestMatchers(
                                "/", "/signup", "/find-password", "/oauth/success", "/main",
                                "/board", "/board/**",
                                "/*.html",
                                "/css/**", "/js/**",
                                "/error/**"
                        ).permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 로그아웃은 인증 필요 (본인의 Refresh Token만 삭제해야 하므로)
                        .requestMatchers("/api/auth/logout").authenticated()

                        // 인증 전 단계에서 호출되는 API
                        .requestMatchers(
                                "/api/members/signup",
                                "/api/members/check-username",
                                "/api/members/email/**",
                                "/api/auth/**"
                        ).permitAll()

                        // OAuth2 로그인 흐름
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()

                        // 그 외 모든 API는 인증 필요
                        .requestMatchers("/api/**").authenticated()

                        // 그 외(정의되지 않은 경로)는 허용 - 404 분기가 처리
                        .anyRequest().permitAll()
                )
                .addFilterBefore(
                        authenticationTokenFilterProvider.getFilter(),
                        UsernamePasswordAuthenticationFilter.class
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuthLoginProvider.getUserService())
                        )
                        .successHandler(oAuthLoginProvider.getSuccessHandler())
                );

        return http.build();
    }
}
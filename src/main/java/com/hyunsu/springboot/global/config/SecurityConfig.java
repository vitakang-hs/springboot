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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
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
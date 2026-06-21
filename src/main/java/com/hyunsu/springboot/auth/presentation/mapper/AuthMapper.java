package com.hyunsu.springboot.auth.presentation.mapper;

import com.hyunsu.springboot.auth.application.command.dto.LoginCommand;
import com.hyunsu.springboot.auth.application.command.dto.LoginCommandResult;
import com.hyunsu.springboot.auth.application.command.dto.ReissueCommandResult;
import com.hyunsu.springboot.auth.presentation.dto.request.LoginRequest;
import com.hyunsu.springboot.auth.presentation.dto.response.LoginResponse;
import com.hyunsu.springboot.auth.presentation.dto.response.ReissueResponse;
import org.springframework.stereotype.Component;

/**
 * presentation의 Request/Response DTO <-> application의 Command/CommandResult 간 변환.
 *
 * - toCommand: Request DTO -> Command (Controller가 Service 호출 전 사용)
 * - toResponse: CommandResult -> Response DTO (Controller가 Service 호출 후 사용)
 *
 * application 레이어(Service, Command/CommandResult)가 presentation 레이어의 타입을
 * 전혀 의존하지 않도록, 모든 변환 책임을 이 Mapper가 가진다.
 */
@Component
public class AuthMapper {

    public LoginCommand toCommand(LoginRequest request, String ipAddress) {
        return new LoginCommand(request.username(), request.password(), ipAddress);
    }

    /**
     * refreshToken은 Response에 포함하지 않는다 (Cookie로 별도 전달되므로 Body에 노출되면 안 됨).
     */
    public LoginResponse toResponse(LoginCommandResult result) {
        return new LoginResponse(result.accessToken(), result.accessTokenExpiresIn());
    }

    public ReissueResponse toResponse(ReissueCommandResult result) {
        return new ReissueResponse(result.accessToken(), result.accessTokenExpiresIn());
    }
}
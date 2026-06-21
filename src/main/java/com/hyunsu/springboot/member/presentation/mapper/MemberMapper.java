package com.hyunsu.springboot.member.presentation.mapper;

import com.hyunsu.springboot.member.application.command.dto.EmailCodeVerifyCommand;
import com.hyunsu.springboot.member.application.command.dto.MemberRegisterCommand;
import com.hyunsu.springboot.member.presentation.dto.request.EmailCodeVerifyRequest;
import com.hyunsu.springboot.member.presentation.dto.request.MemberSignUpRequest;
import org.springframework.stereotype.Component;

/**
 * presentation의 Request DTO를 application의 Command 객체로 변환.
 *
 * Controller가 Request DTO를 Service에 직접 넘기지 않고, 이 Mapper를 거쳐
 * Command로 변환한 뒤 Service에 전달한다. Service가 presentation 레이어의 타입을
 * 직접 의존하지 않도록 분리하기 위함.
 */
@Component
public class MemberMapper {

    public MemberRegisterCommand toCommand(MemberSignUpRequest request) {
        return new MemberRegisterCommand(
                request.username(),
                request.password(),
                request.email()
        );
    }

    public EmailCodeVerifyCommand toCommand(EmailCodeVerifyRequest request) {
        return new EmailCodeVerifyCommand(
                request.email(),
                request.code()
        );
    }
}
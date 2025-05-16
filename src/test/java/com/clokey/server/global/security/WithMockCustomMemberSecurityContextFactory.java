package com.clokey.server.global.security;

import com.clokey.server.domain.member.domain.entity.Member;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithMockCustomMemberSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomMember> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomMember annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Member member = Member.builder()
                .id(annotation.id())
                .email(annotation.email())
                .nickname(annotation.nickname())
                .clokeyId(annotation.clokeyId())
                .bio(annotation.bio())
                .socialType(annotation.socialType())
                .status(annotation.status())
                .registerStatus(annotation.registerStatus())
                .visibility(annotation.visibility())
                .banned(annotation.banned())
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(member, null, List.of());
        context.setAuthentication(auth);
        return context;
    }
}


package com.clokey.server.global.security;

import com.clokey.server.domain.model.entity.enums.MemberStatus;
import com.clokey.server.domain.model.entity.enums.RegisterStatus;
import com.clokey.server.domain.model.entity.enums.SocialType;
import com.clokey.server.domain.model.entity.enums.Visibility;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@WithSecurityContext(factory = WithMockCustomMemberSecurityContextFactory.class)
public @interface WithMockCustomMember {
    long id() default 1L;
    String email() default "test@clokey.com";
    String nickname() default "mockuser";
    String clokeyId() default "mock123";
    String bio() default "테스트 소개";
    SocialType socialType() default SocialType.KAKAO;
    MemberStatus status() default MemberStatus.ACTIVE;
    RegisterStatus registerStatus() default RegisterStatus.NOT_AGREED;
    Visibility visibility() default Visibility.PUBLIC;
    boolean banned() default false;
}


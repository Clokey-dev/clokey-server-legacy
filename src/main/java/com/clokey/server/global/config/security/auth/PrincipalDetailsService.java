package com.clokey.server.global.config.security.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.global.error.code.status.ErrorStatus;
import com.clokey.server.global.error.exception.DatabaseException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final MemberRepositoryService memberRepositoryService;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Member member = memberRepositoryService
                        .getMember(Long.parseLong(userId))
                        .orElseThrow(() -> new DatabaseException(ErrorStatus.NO_SUCH_MEMBER));

        return new PrincipalDetails(member);
    }
}

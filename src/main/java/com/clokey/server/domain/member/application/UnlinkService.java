package com.clokey.server.domain.member.application;


import org.springframework.scheduling.annotation.Async;

public interface UnlinkService {

    @Async
    void asyncDeletedMemberFromES(Long memberId);

    void unlink(Long userId);

    void deleteData(Long userId);
}

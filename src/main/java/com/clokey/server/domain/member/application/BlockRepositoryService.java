package com.clokey.server.domain.member.application;

import com.clokey.server.domain.member.domain.entity.Block;
import com.clokey.server.domain.member.domain.entity.Member;
import org.springframework.data.domain.Pageable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface BlockRepositoryService {

    void delete(Block block);

    void save(Block block);

    boolean existsByBlockerAndBlocked(Long blockerId, Long blockedId);

    Block findByBlockerAndBlocked(Long blockerId, Long blockedId);

    List<Member> findAllByBlocker(Long blockerId, Pageable pageable);

    List<Boolean> checkBlockedStatus(Long blockerId, List<Member> members);

    boolean isBlocking(Member currentUser, Member targetUser);
}

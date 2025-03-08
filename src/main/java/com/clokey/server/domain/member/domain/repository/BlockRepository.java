package com.clokey.server.domain.member.domain.repository;

import com.clokey.server.domain.member.domain.entity.Block;
import com.clokey.server.domain.member.domain.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    @Query("SELECT COUNT(mb) > 0 FROM Block mb WHERE mb.blocker.id = :blockerId AND mb.blocked.id = :blockedId")
    boolean existsByBlockerAndBlocked(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

    @Query("SELECT b FROM Block b WHERE b.blocker.id = :blockerId AND b.blocked.id = :blockedId")
    Optional<Block> findByBlockerAndBlocked(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

    @Query("SELECT b.blocked FROM Block b WHERE b.blocker.id = :blockerId")
    List<Member> findAllByBlocker(@Param("blockerId") Long blockerId, Pageable pageable);

    @Query("SELECT m, CASE WHEN EXISTS (SELECT 1 FROM Block b WHERE b.blocked = m AND b.blocker.id = :blockedId) " +
            "THEN true ELSE false END FROM Member m WHERE m IN :members")
    List<Object[]> findBlockStatus(@Param("blockedId") Long blockedId, @Param("members") List<Member> members);

    @Query("SELECT COUNT(b) > 0 FROM Block b WHERE b.blocker = :currentUser AND b.blocked = :targetUser")
    boolean isBlocking(@Param("currentUser") Member currentUser, @Param("targetUser") Member targetUser);

}


package com.clokey.server.domain.history.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.clokey.server.domain.history.domain.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.history.id = :historyId AND c.comment IS NULL AND c.banned = false")
    Page<Comment> findActiveRootComments(@Param("historyId") Long historyId, PageRequest pageRequest);


    List<Comment> findByCommentId(Long parentId);

    //햐나의 댓글을 기준으로 대댓글을 삭제합니다.
    @Transactional
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.comment.id = :commentId")
    void deleteChildren(@Param("commentId") Long commentId);

    //하나의 기록을 기준으로 대댓글을 모두 삭제합니다.
    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.history.id = :historyId AND c.comment IS NOT NULL")
    void deleteRepliesByHistoryId(@Param("historyId") Long historyId);

    //하나의 기록을 기준으로 댓글을 모두 삭제합니다.
    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.history.id = :historyId")
    void deleteParentCommentsByHistoryId(@Param("historyId") Long historyId);

    boolean existsByIdAndMemberId(Long id, Long memberId);

    boolean existsByIdAndHistoryId(Long id, Long historyId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.history.id = :historyId")
    Long countByHistoryId(@Param("historyId") Long historyId);

    @Modifying
    @Transactional
    @Query("SELECT c.id FROM Comment c WHERE c.history.id IN :historyIds")
    List<Long> selectCommentsByHistoryIds(@Param("historyIds") List<Long> historyIds);


    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.history.id IN :historyIds")
    void deleteCommentsByHistoryIds(@Param("historyIds") List<Long> historyIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.comment.id IN :commentIds")
    void deleteChildrenByHistoryIds(@Param("commentIds") List<Long> commentIds);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.comment.id IN :commentIds")
    void deleteChildrenByCommentIds(@Param("commentIds") List<Long> commentIds);


    @Modifying
    @Query("DELETE FROM Comment c WHERE c.id IN :commentIds")
    void deleteCommentsByCommentIds(@Param("commentIds") List<Long> commentIds);

    Page<Comment> findByMember_Id(Long memberId, PageRequest pageRequest);
}

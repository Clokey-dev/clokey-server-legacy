package com.clokey.server.domain.history.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import com.clokey.server.domain.history.domain.entity.Comment;

public interface CommentRepositoryService {

    Page<Comment> findByHistoryParentCommentsNotBanned(Long historyId, PageRequest pageRequest);

    List<Comment> findByCommentId(Long parentId);

    Comment findById(Long commentId);

    Comment save(Comment comment);

    boolean existsById(Long commentId);

    void deleteChildren(Long commentId);

    void deleteById(Long commentId);

    void deleteAllComments(Long HistoryId);

    boolean existsByIdAndMemberId(Long id, Long memberId);

    boolean existsByIdAndHistoryId(Long id, Long historyId);

    Long countByHistoryId(Long historyId);

    void deleteCommentsByHistoryIds(List<Long> historyId);

    void deleteChildrenByHistoryIds(List<Long> historyIds);

    void deleteChildrenByCommentIds(List<Long> commentIds);

    void deleteCommentsByCommentIds(List<Long> commentIds);

    Page<Comment> findByMemberId(Long memberId, PageRequest pageRequest);

}

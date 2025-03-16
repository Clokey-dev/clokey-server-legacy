package com.clokey.server.domain.history.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.history.domain.entity.Comment;
import com.clokey.server.domain.history.domain.repository.CommentRepository;
import com.clokey.server.global.error.code.status.ErrorStatus;
import com.clokey.server.global.error.exception.DatabaseException;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentRepositoryServiceImpl implements CommentRepositoryService {

    private final CommentRepository commentRepository;

    @Override
    public Page<Comment> findByHistoryIdAndCommentIsNull(Long historyId, PageRequest pageRequest) {
        return commentRepository.findByHistoryIdAndCommentIsNull(historyId, pageRequest);
    }

    @Override
    public List<Comment> findByCommentId(Long parentId) {
        return commentRepository.findByCommentId(parentId);
    }

    @Override
    public Comment findById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new DatabaseException(ErrorStatus.NO_SUCH_COMMENT));
    }

    @Override
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public boolean existsById(Long commentId) {
        return commentRepository.existsById(commentId);
    }

    @Override
    public void deleteChildren(Long commentId) {
        commentRepository.deleteChildren(commentId);
    }

    @Override
    public void deleteById(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteAllComments(Long historyId) {
        commentRepository.deleteRepliesByHistoryId(historyId);
        commentRepository.deleteParentCommentsByHistoryId(historyId);
    }

    @Override
    public boolean existsByIdAndMemberId(Long id, Long memberId) {
        return commentRepository.existsByIdAndMemberId(id, memberId);
    }

    @Override
    public boolean existsByIdAndHistoryId(Long id, Long historyId) {
        return commentRepository.existsByIdAndHistoryId(id, historyId);
    }

    @Override
    public Long countByHistoryId(Long historyId) {
        return commentRepository.countByHistoryId(historyId);
    }

    @Override
    public void deleteCommentsByHistoryIds(List<Long> historyId) {
        commentRepository.deleteCommentsByHistoryIds(historyId);
    }

    @Override
    public void deleteChildrenByHistoryIds(List<Long> historyIds) {

        List<Long> commentIds = commentRepository.selectCommentsByHistoryIds(historyIds);

        commentRepository.deleteChildrenByHistoryIds(commentIds);
    }


    @Override
    public void deleteChildrenByCommentIds(List<Long> commentIds) {
        commentRepository.deleteChildrenByCommentIds(commentIds);
    }

    @Override
    public void deleteCommentsByCommentIds(List<Long> commentIds) {
        commentRepository.deleteCommentsByCommentIds(commentIds);
    }

    @Override
    public Page<Comment> findByMemberId(Long historyId, PageRequest pageRequest) {
        return commentRepository.findByMember_Id(historyId, pageRequest);
    }
}

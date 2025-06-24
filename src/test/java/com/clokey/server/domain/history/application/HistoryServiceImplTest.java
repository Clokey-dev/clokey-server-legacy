package com.clokey.server.domain.history.application;

import com.clokey.server.domain.cloth.domain.repository.ClothRepository;
import com.clokey.server.domain.cloth.exception.validator.ClothAccessibleValidator;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.domain.entity.MemberLike;
import com.clokey.server.domain.history.domain.repository.*;
import com.clokey.server.domain.history.dto.HistoryResponseDTO;
import com.clokey.server.domain.history.exception.validator.HistoryAccessibleValidator;
import com.clokey.server.domain.history.exception.validator.HistoryLikedValidator;
import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.FollowRepository;
import com.clokey.server.domain.member.domain.repository.MemberRepository;
import com.clokey.server.domain.search.application.SearchRepositoryService;
import com.clokey.server.global.infra.s3.S3ImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsInstanceOf.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HistoryServiceImplTest {

    @InjectMocks
    private HistoryServiceImpl historyService;

    @Mock private HistoryLikedValidator historyLikedValidator;
    @Mock private MemberRepositoryService memberRepositoryService;
    @Mock private ClothRepository clothRepository;
    @Mock private ClothAccessibleValidator clothAccessibleValidator;
    @Mock private HistoryAccessibleValidator historyAccessibleValidator;
    @Mock private SearchRepositoryService searchRepositoryService;
    @Mock private HistoryRepository historyRepository;
    @Mock private HistoryImageRepository historyImageRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private S3ImageService s3ImageService;
    @Mock private HistoryClothRepository historyClothRepository;
    @Mock private HashtagHistoryRepository hashtagHistoryRepository;
    @Mock private HashtagRepository hashtagRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private MemberLikeRepository memberLikeRepository;
    @Mock private FollowRepository followRepository;

}

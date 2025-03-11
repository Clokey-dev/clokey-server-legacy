package com.clokey.server.domain.search.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.clokey.server.domain.cloth.converter.ClothConverter;
import com.clokey.server.domain.cloth.domain.document.ClothDocument;
import com.clokey.server.domain.cloth.dto.ClothResponseDTO;
import com.clokey.server.domain.history.converter.HistoryConverter;
import com.clokey.server.domain.history.domain.document.HistoryDocument;
import com.clokey.server.domain.history.dto.HistoryResponseDTO;
import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.member.converter.MemberDocumentConverter;
import com.clokey.server.domain.member.domain.document.MemberDocument;
import com.clokey.server.domain.member.dto.MemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchClient elasticsearchClient;

    private final MemberRepositoryService memberRepositoryService;

    private static final String MEMBER_INDEX_NAME = "user";

    private static final String CLOTH_INDEX_NAME = "cloth";

    private static final String HISTORY_INDEX_NAME = "history";

    private static final Pattern CHOSEONG_PATTERN = Pattern.compile("^[ㄱ-ㅎ]$");

    /****************************************Search Method****************************************/

    // 옷 이름과 브랜드로 검색하는 메서드
    @Override
    public ClothResponseDTO.ClothPreviewListResult searchClothesByNameOrBrand(Long requestedMemberId, String clokeyId, String keyword, int page, int size) throws IOException {

        Pageable pageable = PageRequest.of(page-1, size);

        Long memberId = memberRepositoryService.findMemberByClokeyId(clokeyId).getId();
        boolean isOwner = requestedMemberId.equals(memberId); // 내 계정인지 확인

        SearchResponse<ClothDocument> response = elasticsearchClient.search(s -> s
                        .index(CLOTH_INDEX_NAME)
                        .query(q -> q.bool(b -> {
                            // 특정 멤버의 옷만 필터링
                            b.must(m -> m.term(t -> t.field("memberId").value(memberId)));

                            // 내 계정이 아니면 비공개(visibility: PRIVATE) 옷 제외
                            if (!isOwner) {
                                b.mustNot(m -> m.match(t -> t.field("visibility").query("PRIVATE")));
                            }

                            // 이름 또는 브랜드에서 부분 검색 (OR 조건 적용)
                            b.must(m -> m.bool(bb -> bb
                                    .should(ms -> ms.match(mq -> mq
                                            .field("name")
                                            .query(keyword)
                                            .fuzziness("AUTO")
                                    ))
                                    .should(ms -> ms.matchBoolPrefix(mq -> mq
                                            .field("name")
                                            .query(keyword)
                                            .fuzziness("AUTO")
                                    ))
                                    .should(ms -> ms.matchPhrasePrefix(mq -> mq
                                            .field("name")
                                            .query(keyword)
                                    ))
                                    .should(ms -> ms.match(mq -> mq
                                            .field("brand")
                                            .query(keyword)
                                            .fuzziness("AUTO")
                                    ))
                                    .should(ms -> ms.matchPhrasePrefix(mq -> mq
                                            .field("brand")
                                            .query(keyword)
                                    ))
                            ));
                            return b;
                        }))
                        .from((int) pageable.getOffset())
                        .size(pageable.getPageSize()),
                ClothDocument.class
        );

        List<ClothDocument> results = response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());

        Page<ClothDocument> clothDocuments = new PageImpl<>(results, pageable, response.hits().total().value());

        // Cloth Document -> ClothPreview DTO 변환
        List<ClothResponseDTO.ClothPreview> clothPreviews = ClothConverter.toClothPreviewList(clothDocuments);

        // 페이징 정보를 담아 DTO 반환
        return ClothConverter.toClothPreviewListResult(clothDocuments, clothPreviews);
    }

    // 기록의 해쉬태그와 카테고리로 검색하는 메서드
    @Override
    public HistoryResponseDTO.HistoryPreviewListResult searchHistoriesByHashtagAndCategory(String keyword, int page, int size) throws IOException {

        Pageable pageable = PageRequest.of(page - 1, size);

        SearchResponse<HistoryDocument> response = elasticsearchClient.search(s -> s
                        .index(HISTORY_INDEX_NAME)
                        .query(q -> q.bool(b -> b
                                .must(m -> m.match(t -> t
                                        .field("memberVisibility")
                                        .query("PUBLIC")
                                ))
                                .must(m -> m.match(t -> t
                                        .field("historyVisibility")
                                        .query("PUBLIC")
                                ))
                                .should(m -> m.bool(bb -> bb
                                        .must(ms -> ms.wildcard(mq -> mq
                                                .field("hashtagNames.keyword")
                                                .value("#" + keyword + "*")
                                        ))
                                        .should(ms -> ms.match(mq -> mq
                                                .field("hashtagNames")
                                                .query(keyword)
                                                .fuzziness("AUTO")
                                        ))
                                        .should(ms -> ms.matchPhrasePrefix(mq -> mq
                                                .field("hashtagNames")
                                                .query(keyword)
                                        ))
                                ))
                                .should(m -> m.bool(bb -> bb
                                        .must(ms -> ms.wildcard(mq -> mq
                                                .field("categoryNames.keyword")
                                                .value("*" + keyword + "*")
                                        ))
                                        .should(ms -> ms.matchPhrasePrefix(mq -> mq
                                                .field("categoryNames")
                                                .query(keyword)
                                        ))
                                        .should(ms -> ms.matchBoolPrefix(mq -> mq
                                                .field("categoryNames")
                                                .query(keyword)
                                        ))
                                ))
                                .minimumShouldMatch("1") // 최소 하나는 일치하도록 설정
                        ))
                        .from((int) pageable.getOffset()) // 페이지네이션 적용
                        .size(pageable.getPageSize()),
                HistoryDocument.class
        );

        List<HistoryDocument> results = response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());

        Page<HistoryDocument> historyDocuments = new PageImpl<>(results, pageable, response.hits().total().value());

        // History Document -> HistoryPreview DTO 변환
        List<HistoryResponseDTO.HistoryPreview> historyPreviews = HistoryConverter.toHistoryPreviewList(historyDocuments);

        // 페이징 정보를 담아 DTO 반환
        return HistoryConverter.toHistoryPreviewListResult(historyDocuments, historyPreviews);
    }

    // 유저의 Clokey Id 또는 닉네임으로 검색하는 메서드
    @Override
    public MemberDTO.ProfilePreviewListRP searchMembersByClokeyIdOrNickname(String keyword, int page, int size) throws IOException {

        Pageable pageable = PageRequest.of(page - 1, size);

        if (CHOSEONG_PATTERN.matcher(keyword).find()) {
            return MemberDTO.ProfilePreviewListRP.builder()
                    .profilePreviews(Collections.emptyList()) // 빈 리스트 반환
                    .totalPage(0) // 결과가 없으므로 0 페이지
                    .totalElements(0) // 결과 개수 0
                    .isFirst(true) // 첫 페이지 여부
                    .isLast(true) // 마지막 페이지 여부
                    .build();
        }
        else {
            SearchResponse<MemberDocument> response = elasticsearchClient.search(s -> s
                            .index(MEMBER_INDEX_NAME)
                            .query(q -> q.bool(b -> b
                                    .should(m -> m.bool(bb -> bb
                                            .should(ms -> ms.matchPhrase(t -> t
                                                    .field("nickname")
                                                    .query(keyword)
                                            ))
                                            .should(ms -> ms.matchPhrasePrefix(t -> t
                                                    .field("nickname")
                                                    .query(keyword)
                                            ))
                                            .should(ms -> ms.matchBoolPrefix(t -> t
                                                    .field("nickname")
                                                    .query(keyword)
                                            ))
                                    ))
                                    .should(m -> m.bool(bb -> bb
                                            .should(ms -> ms.matchPhrasePrefix(t -> t
                                                    .field("clokeyId")
                                                    .query(keyword)
                                            ))
                                            .should(ms -> ms.matchBoolPrefix(t -> t
                                                    .field("clokeyId")
                                                    .query(keyword)
                                            ))
                                            .should(ms -> ms.wildcard(t -> t
                                                    .field("clokeyId")
                                                    .value("*" + keyword + "*")
                                            ))
                                    ))
                                    .minimumShouldMatch("1")
                            ))
                            .from((int) pageable.getOffset()) // 페이지네이션 적용
                            .size(pageable.getPageSize()),
                    MemberDocument.class
            );

            List<MemberDocument> results = response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            Page<MemberDocument> memberDocuments = new PageImpl<>(results, pageable, response.hits().total().value());

            List<MemberDTO.ProfilePreview> memberPreviews = MemberDocumentConverter.toProfilePreviewList(memberDocuments);

            return MemberDocumentConverter.toProfilePreviewListRP(memberDocuments, memberPreviews);
        }
    }
}

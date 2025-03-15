package com.clokey.server.domain.search.application;

import java.io.IOException;

import com.clokey.server.domain.cloth.dto.ClothResponseDTO;
import com.clokey.server.domain.history.dto.HistoryResponseDTO;
import com.clokey.server.domain.member.dto.MemberDTO;

public interface SearchService {

    ClothResponseDTO.ClothPreviewListResult searchClothesByNameOrBrand(Long requestedMemberId, String clokeyId, String keyword, int page, int size) throws IOException;

    HistoryResponseDTO.HistoryPreviewListResult searchHistoriesByHashtagAndCategory(Long memberId, String keyword, int page, int size) throws IOException;

    MemberDTO.ProfilePreviewListRP searchMembersByClokeyIdOrNickname(Long memberId, String keyword, int page, int size) throws IOException;
}

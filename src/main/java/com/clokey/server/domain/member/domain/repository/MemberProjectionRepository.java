package com.clokey.server.domain.member.domain.repository;

import com.clokey.server.domain.member.dto.projection.LikedMemberProjectionDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberProjectionRepository {

    @Query("SELECT new com.clokey.server.domain.member.dto.projection.LikedMemberProjectionDTO("
            + "m.id, m.clokeyId, m.profileImageUrl, m.nickname) "
            + "FROM MemberLike ml JOIN ml.member m WHERE ml.history.id = :historyId")
    List<LikedMemberProjectionDTO> findLikedMemberDTOsByHistoryId(@Param("historyId") Long historyId);

}

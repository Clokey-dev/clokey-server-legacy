package com.clokey.server.domain.member.converter;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.dto.MemberDTO;


public class GetUserConverter {

    public static MemberDTO.GetUserRP toGetUserResponseDTO(Member member, Long recordCount, Long followerCount, Long followingCount, Boolean isFollowing
            , List<Cloth> cloths) {
        return MemberDTO.GetUserRP.builder()
                .clokeyId(member.getClokeyId())
                .profileImageUrl(member.getProfileImageUrl())
                .recordCount(recordCount)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .nickname(member.getNickname())
                .bio(member.getBio())
                .profileBackImageUrl(member.getProfileBackImageUrl())
                .visibility(member.getVisibility().toString())
                .isFollowing(isFollowing)
                .clothResults(toGetUserClothResultDTO(cloths))
                .build();
    }

    public static MemberDTO.checkMyselfResult toCheckMyselfResult(Boolean isMe){
        return MemberDTO.checkMyselfResult.builder()
                .isMe(isMe)
                .build();
    }

    public static List<MemberDTO.GetUserClothResult> toGetUserClothResultDTO(List<Cloth> cloths) {
        return cloths.stream()
                .map(cloth -> {
                    if (cloth != null) {
                        return MemberDTO.GetUserClothResult.builder()
                                .clothId(cloth.getId())
                                .clothImage(cloth.getImage().getImageUrl())
                                .build();
                    } else {
                        return MemberDTO.GetUserClothResult.builder()
                                .clothId(null)
                                .clothImage(null)
                                .build();
                    }

                })
                .collect(Collectors.toList());
    }

    public static MemberDTO.GetFollowMemberResult toGetFollowPeopleResultDTO(
            List<Member> members, Pageable pageable, List<Boolean> isFollowings, List<Boolean> isMySelf) {

        List<MemberDTO.FollowMemberResult> memberResults = IntStream.range(0, members.size())
                .mapToObj(i -> convertToProfilePreviewResult(members.get(i), isFollowings.get(i), isMySelf.get(i)))
                .collect(Collectors.toList());

        return MemberDTO.GetFollowMemberResult.builder()
                .members(memberResults)
                .totalPage(pageable.getPageNumber() + 1)
                .totalElements(memberResults.size())
                .isFirst(pageable.getPageNumber() == 0)
                .isLast(memberResults.size() < pageable.getPageSize())
                .build();
    }

    private static MemberDTO.FollowMemberResult convertToProfilePreviewResult(Member member, Boolean isFollowing, Boolean isMySelf) {
        return MemberDTO.FollowMemberResult.builder()
                .profileImage(member.getProfileImageUrl())
                .clokeyId(member.getClokeyId())
                .nickname(member.getNickname())
                .isFollowed(isFollowing)
                .isMe(isMySelf)
                .build();
    }
}

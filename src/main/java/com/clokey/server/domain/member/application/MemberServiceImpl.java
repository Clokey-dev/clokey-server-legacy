package com.clokey.server.domain.member.application;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.cloth.application.ClothRepositoryService;
import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.history.application.HistoryRepositoryService;
import com.clokey.server.domain.member.converter.GetUserConverter;
import com.clokey.server.domain.member.converter.ProfileConverter;
import com.clokey.server.domain.member.domain.entity.Follow;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.dto.MemberDTO;
import com.clokey.server.domain.member.exception.MemberException;
import com.clokey.server.domain.model.entity.enums.RegisterStatus;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.domain.search.application.SearchRepositoryService;
import com.clokey.server.domain.search.exception.SearchException;
import com.clokey.server.global.error.code.status.ErrorStatus;
import com.clokey.server.global.infra.s3.S3ImageService;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepositoryService memberRepositoryService;
    private final FollowRepositoryService followRepositoryService;
    private final HistoryRepositoryService historyRepositoryService;
    private final ClothRepositoryService clothRepositoryService;

    private final S3ImageService s3ImageService; // ✅ S3 업로드 서비스 추가
    private final SearchRepositoryService searchRepositoryService;

    @Override
    @Transactional(readOnly = true)
    public MemberDTO.FollowRP followCheck(String clokeyId, Member currentUser) {
        Long yourUserId = currentUser.getId();
        Long myUserId = memberRepositoryService.findMemberByClokeyId(clokeyId).getId();

        boolean isFollow = followRepositoryService.existsByFollowing_IdAndFollowed_Id(myUserId, yourUserId);

        return new MemberDTO.FollowRP(isFollow);
    }

    @Override
    @Transactional
    public void follow(String clokeyId, Member currentUser) {
        // myClokeyId로 사용자 조회
        Long yourUserId = currentUser.getId();
        Long myUserId = memberRepositoryService.findMemberByClokeyId(clokeyId).getId();

        if (myUserId.equals(yourUserId)) {
            throw new MemberException(ErrorStatus.CANNOT_FOLLOW_MYSELF);
        }

        // 팔로우 관계가 존재하는지 확인
        boolean isFollow = followRepositoryService.existsByFollowing_IdAndFollowed_Id(myUserId, yourUserId);

        if (isFollow) {
            // 팔로우가 이미 존재하면 언팔로우 처리
            Follow follow = followRepositoryService.findByFollowing_IdAndFollowed_Id(myUserId, yourUserId).orElseThrow(() -> new MemberException(ErrorStatus.NO_SUCH_FOLLOWER));

            // 팔로우 삭제 (언팔로우)
            followRepositoryService.delete(follow);
        } else {
            // 팔로우가 존재하지 않으면 팔로우 처리
            Follow follow = Follow.builder().following(memberRepositoryService.findMemberById(myUserId)).followed(memberRepositoryService.findMemberById(yourUserId)).build();

            // 팔로우 저장
            followRepositoryService.save(follow);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public MemberDTO.GetUserRP getUser(String clokeyId, Member currentUser) {
        Member member;
        Boolean isFollowing;
        List<Cloth> topCloths;

        if (clokeyId == null) {
            member = currentUser;
            isFollowing = null;
            topCloths = clothRepositoryService.getTop3Cloths(member);
        } else {
            member = memberRepositoryService.findMemberByClokeyId(clokeyId);
            isFollowing = followRepositoryService.isFollowing(currentUser, member);
            if (member.getVisibility().equals(Visibility.PUBLIC)) {
                topCloths = clothRepositoryService.getTop3PublicCloths(member);
            } else {
                topCloths = Arrays.asList(null, null, null);

            }
        }

        Long recordCount = historyRepositoryService.countHistoryByMember(member);
        Long followerCount = followRepositoryService.countFollowersByMember(member);
        Long followingCount = followRepositoryService.countFollowingByMember(member);

        return GetUserConverter.toGetUserResponseDTO(member, recordCount, followerCount, followingCount, isFollowing, topCloths);
    }


    @Override
    @Transactional
    public MemberDTO.ProfileRP updateProfile(Long userId, MemberDTO.ProfileRQ request, MultipartFile profileImage, MultipartFile profileBackImage) {
        // 사용자 확인
        Member member = memberRepositoryService.findMemberById(userId);

        // ✅ S3 업로드 후 URL 저장
        String profileImageUrl;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = s3ImageService.upload(profileImage);
        } else {
            profileImageUrl = member.getProfileImageUrl();
        }

        String profileBackImageUrl;
        if (profileBackImage != null && !profileBackImage.isEmpty()) {
            profileBackImageUrl = s3ImageService.upload(profileBackImage);
        } else {
            profileBackImageUrl = member.getProfileBackImageUrl();
        }


        member.profileUpdate(request, profileImageUrl, profileBackImageUrl);

        if (member.getRegisterStatus() != RegisterStatus.REGISTERED) {
            // 약관 동의가 완료되었으므로 회원의 등록 상태를 업데이트
            member.updateRegisterStatus(RegisterStatus.REGISTERED);
        }

        // 저장
        Member updatedMember = memberRepositoryService.saveMember(member);

        // ES 동기화
        try {
            searchRepositoryService.updateMemberDataToElasticsearch(updatedMember);
        } catch (IOException e) {
            throw new SearchException(ErrorStatus.ELASTIC_SEARCH_SYNC_FAULT);
        }

        // 응답 생성
        return ProfileConverter.toProfileRPDTO(updatedMember);
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        Member member = memberRepositoryService.findMemberById(userId);
        member.deleteAccessRefreshToken();
    }

    @Override
    @Transactional(readOnly = true)
    public void clokeyIdUsingCheck(String clokeyId, Member currentUser) {
        // 현재 로그인한 사용자의 clokeyId 가져오기
        String myClokeyId = currentUser.getClokeyId();

        // 1️⃣ 내 아이디가 없으면 입력한 아이디가 중복인지 검사
        if (myClokeyId == null) {
            if (memberRepositoryService.existsByClokeyId(clokeyId)) {
                throw new MemberException(ErrorStatus.DUPLICATE_CLOKEY_ID);
            }
            return;
        }

        // 2️⃣ 내 아이디가 존재하면, 내가 입력한 아이디가 기존 내 아이디와 다를 때만 중복 검사
        if (!clokeyId.equals(myClokeyId) && memberRepositoryService.existsByClokeyId(clokeyId)) {
            throw new MemberException(ErrorStatus.DUPLICATE_CLOKEY_ID);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public MemberDTO.GetFollowMemberResult getFollowPeople(Long memberId, String clokeyId, Integer page, Boolean isFollow) {
        // clokeyId로 계정 공개 여부 가져오기
        Member findMember = memberRepositoryService.findByClokeyId(clokeyId);

        Pageable pageable = PageRequest.of(page - 1, 10);
        if (findMember.getVisibility() == Visibility.PUBLIC) {
            if (isFollow) {
                // 팔로잉 리스트 가져오기
                List<Member> members = followRepositoryService.findFollowingByFollowedId(findMember.getId(), pageable);
                List<Boolean> isFollowings = followRepositoryService.checkFollowingStatus(memberId, members);
                List<Boolean> isMySelf = members.stream().map(member -> member.getId().equals(memberId)).toList();
                return GetUserConverter.toGetFollowPeopleResultDTO(members, pageable, isFollowings, isMySelf);
            } else {
                // 팔로워 리스트 가져오기
                List<Member> members = followRepositoryService.findFollowedByFollowingId(findMember.getId(), pageable);
                List<Boolean> isFollowings = followRepositoryService.checkFollowingStatus(memberId, members);
                List<Boolean> isMySelf = members.stream()
                        .map(member -> member.getId().equals(memberId))
                        .toList();
                return GetUserConverter.toGetFollowPeopleResultDTO(members, pageable, isFollowings,isMySelf);
            }
        }
        return GetUserConverter.toGetFollowPeopleResultDTO(new ArrayList<>(), pageable, new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public MemberDTO.checkMyselfResult checkMyself(String myClokeyId, String checkClokeyId) {
        return GetUserConverter.toCheckMyselfResult(myClokeyId.equals(checkClokeyId));
    }

}

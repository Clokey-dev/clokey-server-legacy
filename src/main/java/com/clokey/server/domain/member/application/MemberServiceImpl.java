package com.clokey.server.domain.member.application;

import com.clokey.server.domain.member.domain.entity.Block;
import com.clokey.server.domain.member.domain.entity.ProfileReport;
import com.clokey.server.domain.member.domain.repository.BlockRepository;
import com.clokey.server.domain.member.domain.repository.MemberRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepositoryService memberRepositoryService;
    private final FollowRepositoryService followRepositoryService;
    private final HistoryRepositoryService historyRepositoryService;
    private final ClothRepositoryService clothRepositoryService;
    private final BlockRepositoryService blockRepositoryService;
    private final ProfileReportRepositoryService profileReportRepositoryService;

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
        Boolean isBlocking;
        List<Cloth> topCloths;

        if (clokeyId == null) {
            member = currentUser;
            isFollowing = null;
            isBlocking = null;
            topCloths = clothRepositoryService.getTop3Cloths(member);
        } else {
            member = memberRepositoryService.findMemberByClokeyId(clokeyId);
            isFollowing = followRepositoryService.isFollowing(currentUser, member);
            isBlocking = blockRepositoryService.isBlocking(currentUser, member);
            if (member.getVisibility().equals(Visibility.PUBLIC)) {
                topCloths = clothRepositoryService.getTop3PublicCloths(member);
            } else {
                topCloths = Arrays.asList(null, null, null);

            }
        }

        Long recordCount = historyRepositoryService.countHistoryByMember(member);
        Long followerCount = followRepositoryService.countFollowersByMember(member);
        Long followingCount = followRepositoryService.countFollowingByMember(member);

        return GetUserConverter.toGetUserResponseDTO(member, recordCount, followerCount, followingCount, isFollowing, isBlocking, topCloths);
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


    //회원 차단 로직
    @Override
    @Transactional
    public void blockMember(String clokeyId, Member currentUser) {
        Member blocker = currentUser;
        Member blocked = memberRepositoryService.findMemberByClokeyId(clokeyId);

        // 본인을 차단할 수 없음
        if (blocker.getId().equals(blocked.getId())) {
            throw new MemberException(ErrorStatus.CANNOT_BLOCK_MYSELF);
        }

        // 이미 차단한 경우 차단 해제 (토글 방식 유지)
        if (blockRepositoryService.existsByBlockerAndBlocked(blocker.getId(), blocked.getId())) {
            Block block = blockRepositoryService.findByBlockerAndBlocked(blocker.getId(), blocked.getId());
            blockRepositoryService.delete(block);
            return; // 차단 해제 후 종료
        }

        // 팔로우 관계 제거
        if(followRepositoryService.existsByFollowing_IdAndFollowed_Id(blocker.getId(), blocked.getId())){
            Follow follow = followRepositoryService.findByFollowing_IdAndFollowed_Id(blocker.getId(), blocked.getId()).orElseThrow(() -> new MemberException(ErrorStatus.NO_SUCH_FOLLOWER));
            followRepositoryService.delete(follow);
        }
        if(followRepositoryService.existsByFollowing_IdAndFollowed_Id(blocked.getId(), blocker.getId())){
            Follow follow = followRepositoryService.findByFollowing_IdAndFollowed_Id(blocked.getId(), blocker.getId()).orElseThrow(() -> new MemberException(ErrorStatus.NO_SUCH_FOLLOWER));
            followRepositoryService.delete(follow);
        }

        // 차단 정보 추가
        Block block = Block.builder().blocker(blocker).blocked(blocked).build();
        blockRepositoryService.save(block);
    }



    @Override
    @Transactional(readOnly = true)
    public MemberDTO.GetBlockMemberResult getBlockedMembers(Member currentUser, Integer page) {
        Pageable pageable = PageRequest.of(page - 1, 10);

            List<Member> members = blockRepositoryService.findAllByBlocker(currentUser.getId(), pageable);
            List<Boolean> isBlocked = blockRepositoryService.checkBlockedStatus(currentUser.getId(), members);
            List<Boolean> isMySelf = members.stream().map(member -> member.getId().equals(currentUser.getId())).toList();
            return GetUserConverter.toGetBlockPeopleResultDTO(members, pageable, isBlocked, isMySelf);
    }

    @Override
    @Transactional
    public void reportMember(String clokeyId, Member currentUser, MemberDTO.ReportRQ request) {
        Member target = memberRepositoryService.findMemberByClokeyId(clokeyId);

        if (currentUser.getId().equals(target.getId())) {
            throw new MemberException(ErrorStatus.CANNOT_REPORT_MYSELF);
        }

        ProfileReport report = ProfileReport.builder().
                reporter(currentUser).
                reported(target).
                type(request.getType()).
                otherType(request.getOtherType()).
                reason(request.getReason()).
                reportedAt(LocalDateTime.now()).build();

        profileReportRepositoryService.save(report);
    }

}

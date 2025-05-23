package com.clokey.server.domain.member.application;

import com.clokey.server.domain.cloth.domain.repository.ClothRepository;
import com.clokey.server.domain.history.domain.repository.HistoryRepository;
import com.clokey.server.domain.member.domain.entity.Block;
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

import com.clokey.server.domain.cloth.domain.entity.Cloth;
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
    private final HistoryRepository historyRepository;
    private final BlockRepositoryService blockRepositoryService;
    private final ClothRepository clothRepository;

    private final S3ImageService s3ImageService; // ✅ S3 업로드 서비스 추가
    private final SearchRepositoryService searchRepositoryService;

    private static final String FAILED_ES_UPDATE_SYNC_USER_KEY = "failed_es_update_sync_user";

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
        Boolean isMyself;
        List<Cloth> topCloths;

        if (clokeyId == null || clokeyId.equals(currentUser.getClokeyId())) {
            member = currentUser;
            isFollowing = null;
            isBlocking = null;
            isMyself = true;
            topCloths = getTop3Cloths(member);
        } else {
            member = memberRepositoryService.findMemberByClokeyId(clokeyId);
            isFollowing = followRepositoryService.isFollowing(currentUser, member);
            isBlocking = blockRepositoryService.isBlocking(currentUser, member);
            isMyself = member.getId().equals(currentUser.getId());
            if (member.getVisibility().equals(Visibility.PUBLIC)) {
                topCloths = getTop3PublicCloths(member);
            } else {
                topCloths = Arrays.asList(null, null, null);

            }
        }

        Long recordCount = historyRepository.countHistoryByMember(member);
        Long followerCount = followRepositoryService.countFollowersByMember(member);
        Long followingCount = followRepositoryService.countFollowingByMember(member);

        return GetUserConverter.toGetUserResponseDTO(member, recordCount, followerCount, followingCount, isFollowing, isBlocking, isMyself, topCloths);
    }


    @Override
    @Transactional
    public MemberDTO.ProfileRP updateProfile(Long userId, MemberDTO.ProfileRQ request, MultipartFile profileImage, MultipartFile profileBackImage) {
        // 사용자 확인
        Member member = memberRepositoryService.findMemberById(userId);

        validateVisualizeBannedMember(member,request);

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
        asyncUpdatedMemberFromES(updatedMember);

        // 응답 생성
        return ProfileConverter.toProfileRPDTO(updatedMember);
    }

    private void validateVisualizeBannedMember(Member member, MemberDTO.ProfileRQ request){
        boolean banned = member.isBanned();
        boolean changeToPublic = request.getVisibility().equals(Visibility.PUBLIC);
        if(banned && changeToPublic){
            throw new MemberException(ErrorStatus.BANNED_MEMBER_TO_PUBLIC);
        }
    }

    // 비동기 방식으로 Elasticsearch 수정 요청
    public void asyncUpdatedMemberFromES(Member member) {
        try {
            searchRepositoryService.updateMemberDataToElasticsearch(member);
        } catch (IOException e) {
            searchRepositoryService.saveFailedUpdateES(member,FAILED_ES_UPDATE_SYNC_USER_KEY); // 실패한 Member 저장 후 재시도 가능하도록 처리
            throw new SearchException(ErrorStatus.ELASTIC_SEARCH_SYNC_FAULT);
        }
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
        if (findMember.getId().equals(memberId) || findMember.getVisibility() == Visibility.PUBLIC) {
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
            return GetUserConverter.toGetBlockPeopleResultDTO(members, pageable);
    }

    @Override
    public MemberDTO.checkMyselfResult checkMyself(String myClokeyId, String checkClokeyId) {
        return GetUserConverter.toCheckMyselfResult(myClokeyId.equals(checkClokeyId));
    }

    private List<Cloth> getTop3Cloths(Member member){
        List<Cloth> cloths = clothRepository.getTop3Cloths(member);
        while (cloths.size() < 3) {
            cloths.add(null);
        }
        return cloths;
    }

    private List<Cloth> getTop3PublicCloths(Member member) {
        List<Cloth> cloths = clothRepository.getTop3PublicCloths(member);
        while (cloths.size() < 3) {
            cloths.add(null);
        }
        return cloths;
    }

}

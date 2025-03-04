package com.clokey.server.domain.member.application;

import org.springframework.web.multipart.MultipartFile;

import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.dto.MemberDTO;

public interface MemberService {
    void follow(String clokeyId, Member currentUser);

    MemberDTO.FollowRP followCheck(String clokeyId, Member currentUser);

    MemberDTO.GetUserRP getUser(String clokeyId, Member currentUser); // 로그인한 사용자 정보 추가

    MemberDTO.ProfileRP updateProfile(Long userId, MemberDTO.ProfileRQ request, MultipartFile profileImage, MultipartFile profileBackImage);

    void logout(Long userId);

    void clokeyIdUsingCheck(String clokeyId, Member currentUser);

    MemberDTO.GetFollowMemberResult getFollowPeople(Long memberId, String clokeyId, Integer page, Boolean isFollow);

    MemberDTO.checkMyselfResult checkMyself(String myClokeyId, String checkClokeyId);
}

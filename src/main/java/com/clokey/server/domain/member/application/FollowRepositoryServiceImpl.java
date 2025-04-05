package com.clokey.server.domain.member.application;

import com.clokey.server.domain.member.domain.entity.Follow;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.FollowRepository;
import com.clokey.server.domain.model.entity.enums.Visibility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.member.domain.entity.Follow;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.FollowRepository;

@Service
@RequiredArgsConstructor
public class FollowRepositoryServiceImpl implements FollowRepositoryService {

    private final FollowRepository followRepository;

    @Override
    public List<Boolean> checkFollowingStatus(Long followedId, List<Member> members) {
        List<Object[]> results = followRepository.findFollowingStatus(followedId, members);

        Map<Member, Boolean> statusMap = new LinkedHashMap<>();
        for (Object[] result : results) {
            statusMap.put((Member) result[0], (Boolean) result[1]);
        }

        return members.stream()
                .map(member -> statusMap.getOrDefault(member, false))
                .toList();
    }

    @Override
    public List<Boolean> checkFollowedStatus(Long followingId, List<Member> members) {
        return followRepository.checkFollowedStatus(followingId, members);
    }

    @Override
    public List<Member> findFollowedByFollowingId(Long followingId) {
        return followRepository.findFollowedByFollowingId(followingId);
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        followRepository.deleteByMemberId(memberId);
    }

    public boolean existsByFollowing_IdAndFollowed_Id(Long followingId, Long followedId) {
        return followRepository.existsByFollowing_IdAndFollowed_Id(followingId, followedId);
    }

    @Override
    public List<Member> findFollowingByFollowedId(Long followedId) {
        return followRepository.findFollowingByFollowedId(followedId);
    }

    @Override
    public Optional<Follow> findByFollowing_IdAndFollowed_Id(Long followingId, Long followedId) {
        return followRepository.findByFollowing_IdAndFollowed_Id(followingId, followedId);
    }

    @Override
    public void delete(Follow follow) {
        followRepository.delete(follow);
    }

    @Override
    public void save(Follow follow) {
        followRepository.save(follow);
    }

    @Override
    public boolean isFollowing(Member currentUser, Member targetUser) {
        return followRepository.isFollowing(currentUser, targetUser);
    }

    @Override
    public Long countFollowersByMember(Member member) {
        return followRepository.countFollowersByMember(member);
    }

    @Override
    public Long countFollowingByMember(Member member) {
        return followRepository.countFollowingByMember(member);
    }

    @Override
    public List<Member> findFollowedByFollowingId(Long followingId, Pageable pageable) {
        return followRepository.findFollowedByFollowingId(followingId, pageable).stream()
                .filter(member -> member.getVisibility() == Visibility.PUBLIC)
                .toList();
    }

    @Override
    public List<Member> findFollowingByFollowedId(Long followedId, Pageable pageable) {
        return followRepository.findFollowingByFollowedId(followedId, pageable).stream()
                .filter(member -> member.getVisibility() == Visibility.PUBLIC)
                .toList();
    }

    @Override
    public List<Long> findTopFollowingMembers() {
        return followRepository.findTopFollowingMembers(PageRequest.of(0, 5));
    }
}

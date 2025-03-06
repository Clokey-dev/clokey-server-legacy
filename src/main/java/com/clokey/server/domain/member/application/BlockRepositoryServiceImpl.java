package com.clokey.server.domain.member.application;

import com.clokey.server.domain.member.domain.entity.Block;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BlockRepositoryServiceImpl implements BlockRepositoryService {

        private final BlockRepository blockRepository;

        public void delete(Block block) {
            blockRepository.delete(block);
        }

        public void save(Block block) {
            blockRepository.save(block);
        }

        public boolean existsByBlockerAndBlocked(Long blockerId, Long blockedId) {
            return blockRepository.existsByBlockerAndBlocked(blockerId, blockedId);
        }

        public Block findByBlockerAndBlocked(Long blockerId, Long blockedId) {
            return blockRepository.findByBlockerAndBlocked(blockerId, blockedId).orElse(null);
        }

        public List<Member> findAllByBlocker(Long blockerId, Pageable pageable) {
            return blockRepository.findAllByBlocker(blockerId, pageable);
        }

    @Override
    public List<Boolean> checkBlockedStatus(Long blockerId, List<Member> members) {
        List<Object[]> results = blockRepository.findBlockStatus(blockerId, members);

        Map<Member, Boolean> statusMap = new LinkedHashMap<>();
        for (Object[] result : results) {
            statusMap.put((Member) result[0], (Boolean) result[1]);
        }

        return members.stream()
                .map(member -> statusMap.getOrDefault(member, false))
                .toList();
    }

    @Override
    public boolean isBlocking(Member currentUser, Member targetUser) {
        return blockRepository.isBlocking(currentUser, targetUser);
    }

}

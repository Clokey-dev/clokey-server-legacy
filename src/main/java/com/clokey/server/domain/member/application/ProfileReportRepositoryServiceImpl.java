package com.clokey.server.domain.member.application;

import com.clokey.server.domain.member.domain.entity.ProfileReport;
import com.clokey.server.domain.member.domain.repository.ProfileReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileReportRepositoryServiceImpl implements ProfileReportRepositoryService {

    private final ProfileReportRepository profileReportRepository;

    @Override
    public void save(ProfileReport profileReport) {
        profileReportRepository.save(profileReport);
    }
}

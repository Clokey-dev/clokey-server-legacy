package com.clokey.server.domain.report.application;

import com.clokey.server.domain.report.domain.entity.ProfileReport;
import com.clokey.server.domain.report.domain.repository.ProfileReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileReportRepositoryServiceImpl implements ProfileReportRepositoryService {

    private final ProfileReportRepository profileReportRepository;

    @Override
    public Long save(ProfileReport profileReport) {
        return profileReportRepository.save(profileReport).getId();
    }
}

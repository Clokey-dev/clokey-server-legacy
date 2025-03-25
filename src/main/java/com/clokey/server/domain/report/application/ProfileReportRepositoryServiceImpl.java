package com.clokey.server.domain.report.application;

import com.clokey.server.domain.report.domain.entity.ProfileReport;
import com.clokey.server.domain.report.domain.repository.ProfileReportRepository;
import com.clokey.server.domain.report.exception.ReportException;
import com.clokey.server.global.error.code.status.ErrorStatus;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.ProviderException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileReportRepositoryServiceImpl implements ProfileReportRepositoryService {

    private final ProfileReportRepository profileReportRepository;

    @Override
    public Long save(ProfileReport profileReport) {
        return profileReportRepository.save(profileReport).getId();
    }

    @Override
    public List<ProfileReport> findAllByPredicate(Predicate predicate) {
        return profileReportRepository.findAll(predicate);
    }

    @Override
    public ProfileReport findById(Long id) {
        return profileReportRepository.findById(id).orElseThrow(()-> new ReportException(ErrorStatus.NO_SUCH_MEMBER_REPORT));
    }
}

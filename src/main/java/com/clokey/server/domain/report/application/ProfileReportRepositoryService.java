package com.clokey.server.domain.report.application;

import com.clokey.server.domain.report.domain.entity.ProfileReport;

public interface ProfileReportRepositoryService {
    Long save(ProfileReport profileReport);
}

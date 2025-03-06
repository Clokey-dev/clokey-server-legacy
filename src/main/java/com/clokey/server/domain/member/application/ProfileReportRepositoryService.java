package com.clokey.server.domain.member.application;

import com.clokey.server.domain.member.domain.entity.ProfileReport;

public interface ProfileReportRepositoryService {
    void save(ProfileReport profileReport);
}

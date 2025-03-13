package com.clokey.server.domain.report.application;

import com.clokey.server.domain.report.domain.entity.ProfileReport;
import com.querydsl.core.types.Predicate;
import java.util.List;

public interface ProfileReportRepositoryService {
    Long save(ProfileReport profileReport);

    List<ProfileReport> findAllByPredicate(Predicate predicate);
}

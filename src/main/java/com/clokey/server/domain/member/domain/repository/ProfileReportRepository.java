package com.clokey.server.domain.member.domain.repository;

import com.clokey.server.domain.member.domain.entity.ProfileReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileReportRepository extends JpaRepository<ProfileReport, Long> {
}

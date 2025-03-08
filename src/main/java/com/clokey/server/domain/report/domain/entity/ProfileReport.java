package com.clokey.server.domain.report.domain.entity;

import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.BaseEntity;
import com.clokey.server.domain.model.entity.enums.ProfileReportType;
import com.clokey.server.domain.model.entity.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "profile_report")
public class ProfileReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // Member와의 다대일 관계 설정
    @JoinColumn(name = "reporter_id", nullable = false)  // 기자 ID
    private Member reporter;  // 신고자

    @ManyToOne(fetch = FetchType.LAZY)  // Member와의 다대일 관계 설정
    @JoinColumn(name = "reported_id", nullable = false)  // 신고된 ID
    private Member reported;  // 신고된 대상

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileReportType profileReportType;  // 신고 타입

    @Column(length = 200)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15) DEFAULT 'UNCHECKED'", nullable = false)
    private ReportStatus reportStatus;

}

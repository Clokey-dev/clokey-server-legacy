package com.clokey.server.domain.member.domain.entity;

import com.clokey.server.domain.model.entity.BaseEntity;
import com.clokey.server.domain.model.entity.enums.HistoryReport;
import com.clokey.server.domain.model.entity.enums.ProfileReport;
import com.clokey.server.domain.model.entity.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "report")
public class Report extends BaseEntity {

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
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    private ProfileReport profileReportContent;

    @Enumerated(EnumType.STRING)
    private HistoryReport historyReportContent;

    @Column
    private String otherContent;

    private String reason;  // 신고 사유

    private LocalDateTime reportedAt = LocalDateTime.now();  // 신고 시간

}

package com.clokey.server.domain.report.domain.entity;

import com.clokey.server.domain.history.domain.entity.Comment;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.BaseEntity;
import com.clokey.server.domain.model.entity.enums.CommentReportType;
import com.clokey.server.domain.model.entity.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CommentReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentReportType commentReportType;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15) DEFAULT 'UNCHECKED'", nullable = false)
    private ReportStatus reportStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 200)
    private String content;

    public void approveReport(){
        this.reportStatus = ReportStatus.APPROVED;
    }

    public void disApproveReport(){
        this.reportStatus = ReportStatus.DISAPPROVED;
    }
}

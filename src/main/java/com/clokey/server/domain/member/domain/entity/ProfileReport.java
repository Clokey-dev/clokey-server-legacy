package com.clokey.server.domain.member.domain.entity;

import com.clokey.server.domain.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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
    private com.clokey.server.domain.model.entity.enums.ProfileReport type;  // 신고 타입

    @Column
    private String otherType;

    @Column(length = 200)
    private String reason;  // 신고 사유

    @Column
    private LocalDateTime reportedAt = LocalDateTime.now();  // 신고 시간

}

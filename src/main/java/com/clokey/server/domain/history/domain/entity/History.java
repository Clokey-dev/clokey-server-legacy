package com.clokey.server.domain.history.domain.entity;

import java.time.LocalDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import lombok.*;

import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.BaseEntity;
import com.clokey.server.domain.model.entity.enums.Visibility;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "history",
        indexes = {
                @Index(name = "idx_member_date", columnList = "member_id, history_date"),
                @Index(name = "idx_member_id_id", columnList = "member_id, id")
        }
)
public class History extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate historyDate;

    @Min(0)
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int likes;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15) DEFAULT 'PUBLIC'", nullable = false) // 공개 범위
    private Visibility visibility;

    @Column(length = 200)
    private String content;

    //default가 false입니다.
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean banned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void updateHistory(String content, Visibility visibility) {
        if (content != null) {
            this.content = content;
        }
        if (visibility != null) {
            this.visibility = visibility;
        }
    }

    public void makePublic(){
        this.visibility = Visibility.PUBLIC;
    }

    public void makePrivate(){
        this.visibility = Visibility.PRIVATE;
    }

    public void ban(){
        this.banned = true;
    }

    public void releaseBan(){
        this.banned = false;
    }

}

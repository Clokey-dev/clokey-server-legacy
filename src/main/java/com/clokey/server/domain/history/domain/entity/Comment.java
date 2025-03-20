package com.clokey.server.domain.history.domain.entity;

import jakarta.persistence.*;

import lombok.*;

import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = false)
    private History history;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment comment;

    //default가 false입니다.
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean banned;

    public void updateContent(String content) {
        if (content != null && !content.isEmpty()) {
            this.content = content;
        }
    }

    public void ban(){
        this.banned = true;
    }

    public void releaseBan(){
        this.banned = false;
    }
}

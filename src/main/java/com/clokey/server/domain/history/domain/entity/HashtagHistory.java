package com.clokey.server.domain.history.domain.entity;

import jakarta.persistence.*;

import lombok.*;

import com.clokey.server.domain.model.entity.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        indexes = {
                @Index(name = "idx_history_id", columnList = "history_id"),
                @Index(name = "idx_hashtag_id", columnList = "hashtag_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_history_hashtag", columnNames = {"history_id", "hashtag_id"})
        }
)
public class HashtagHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id", nullable = false)
    private Hashtag hashtag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = false)
    private History history;
}

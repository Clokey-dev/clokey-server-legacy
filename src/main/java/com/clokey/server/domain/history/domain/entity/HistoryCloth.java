package com.clokey.server.domain.history.domain.entity;

import jakarta.persistence.*;

import lombok.*;

import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.model.entity.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        indexes = {
                @Index(name = "idx_history_cloth_history_id", columnList = "history_id")
        }
)
public class HistoryCloth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = false)
    private History history;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloth_id", nullable = false)
    private Cloth cloth;
}

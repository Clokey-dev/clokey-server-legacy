package com.clokey.server.domain.cloth.domain.entity;

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
                @Index(name = "idx_cloth_image_cloth_id", columnList = "cloth_id")
        }
)
public class ClothImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl; // 옷 이미지 URL

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloth_id", nullable = false)
    private Cloth cloth;

    public void updateClothImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }
}

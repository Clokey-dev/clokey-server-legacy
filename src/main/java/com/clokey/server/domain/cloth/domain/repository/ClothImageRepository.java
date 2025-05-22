package com.clokey.server.domain.cloth.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.clokey.server.domain.cloth.domain.entity.ClothImage;

public interface ClothImageRepository extends JpaRepository<ClothImage, Long> , ClothProjectionRepository {

    int deleteByClothId(Long clothId);

    ClothImage findByClothId(Long clothId);

    List<ClothImage> findByCloth_IdIn(List<Long> clothIds);

    @Modifying
    @Query("DELETE FROM ClothImage c WHERE c.cloth.id IN :clothIds")
    void deleteByClothIds(@Param("clothIds") List<Long> clothIds);
}

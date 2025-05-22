package com.clokey.server.domain.history.application;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.domain.entity.HistoryImage;
import com.clokey.server.domain.history.domain.repository.HistoryImageRepository;
import com.clokey.server.global.error.code.status.ErrorStatus;
import com.clokey.server.global.error.exception.DatabaseException;
import com.clokey.server.global.infra.s3.S3ImageService;

@Transactional
@Service
@RequiredArgsConstructor
public class HistoryImageRepositoryServiceImpl implements HistoryImageRepositoryService {

    private final HistoryImageRepository historyImageRepository;
    private final S3ImageService s3ImageService;

    @Override
    public List<HistoryImage> findByHistoryId(Long historyId) {
        return historyImageRepository.findByHistory_Id(historyId);
    }

    @Override
    public void save(MultipartFile image, History history) {
        if (image == null || image.isEmpty()) {
            throw new DatabaseException(ErrorStatus.S3_EMPTY_FILE_EXCEPTION);
        }
        String url = s3ImageService.upload(image);

        historyImageRepository.save(HistoryImage.builder()
                .imageUrl(url)
                .history(history)
                .build());
    }

    @Override
    public void save(List<MultipartFile> images, History history) {
        if (images == null || images.isEmpty()) {
            throw new DatabaseException(ErrorStatus.S3_EMPTY_FILE_EXCEPTION);
        }

        images.forEach(image -> save(image, history));

    }

    @Override
    public void deleteAllByHistoryId(Long historyId) {

        List<HistoryImage> historyImages = historyImageRepository.findByHistory_Id(historyId);

        if (historyImages == null || historyImages.isEmpty()) {
            return;
        }


        historyImages.forEach(image -> {
            s3ImageService.deleteImageFromS3(image.getImageUrl());
            historyImageRepository.delete(image);
        });
    }

    @Override
    public void deleteAllByHistoryIds(List<Long> historyIds) {
        List<HistoryImage> historyImages = historyImageRepository.findByHistory_IdIn(historyIds);

        if (historyImages == null || historyImages.isEmpty()) {
            return;
        }

        historyImages.forEach(image -> s3ImageService.deleteImageFromS3(image.getImageUrl()));

        historyImageRepository.deleteByHistoryIds(historyIds);
    }


    public List<HistoryImage> findByHistoryIdIn(List<Long> historyIds) {
        return historyImageRepository.findByHistoryIdIn(historyIds);
    }

    @Override
    public Map<Long, String> findFirstImagesByHistoryIds(List<Long> historyIds) {
        if (historyIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<HistoryImage> result = historyImageRepository.findByHistoryIdIn(historyIds);

        return result.stream()
                .collect(Collectors.toMap(
                        hi -> hi.getHistory().getId(),
                        HistoryImage::getImageUrl,
                        (existing, replacement) -> existing
                ));
    }
}

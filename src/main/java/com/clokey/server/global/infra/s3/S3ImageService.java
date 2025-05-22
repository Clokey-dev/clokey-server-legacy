package com.clokey.server.global.infra.s3;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3ImageService {
    String upload(MultipartFile image);
    void deleteImageFromS3(String imageAddress);
    List<String> uploadAll(List<MultipartFile> images);
    void deleteAllFromS3(List<String> imageAddresses);
}

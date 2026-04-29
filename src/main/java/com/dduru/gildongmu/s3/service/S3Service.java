package com.dduru.gildongmu.s3.service;

import com.dduru.gildongmu.s3.dto.response.ImageUploadResponse;
import com.dduru.gildongmu.s3.exception.InvalidFileExtensionException;
import com.dduru.gildongmu.common.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "aws.s3", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final String S3_POSTS_DIR = "posts/";
    private static final String S3_PROFILES_DIR = "profiles/";
    private static final String S3_CHATS_DIR = "chats/";
    /*private static final String S3_SURVEY_DIR = "survey/";*/
    private static final Duration PRESIGNED_URL_TTL = Duration.ofMinutes(10);

    public List<ImageUploadResponse> preparePostImageUpload(List<String> fileNames) {
        List<ImageUploadResponse> responses = fileNames.stream()
                .map(fileName -> prepareUploadInternal(fileName, S3_POSTS_DIR))
                .toList();
        log.info("Presigned URL 생성 완료{} 파일 개수: {}", S3_POSTS_DIR, responses.size());
        return responses;
    }


    public List<ImageUploadResponse> prepareProfileImageUpload(List<String> fileNames) {
        List<ImageUploadResponse> responses = fileNames.stream()
                .map(fileName -> prepareUploadInternal(fileName, S3_PROFILES_DIR))
                .toList();
        log.info("Presigned URL 생성 완료{} 파일 개수: {}", S3_PROFILES_DIR, responses.size());
        return responses;
    }

   /* public List<ImageUploadResponse> prepareSurveyImageUpload(List<String> fileNames) {
        log.debug("Presigned URL 생성 시작(survey) - 파일 개수: {}", fileNames.size());
        List<ImageUploadResponse> responses = fileNames.stream()
                .map(fileName -> prepareUploadInternal(fileName, S3_SURVEY_DIR, false))
                .toList();
        log.info("Presigned URL 생성 완료(survey) - 파일 개수: {}", responses.size());
        return responses;
    }*/


    public List<ImageUploadResponse> prepareChatImageUpload(List<String> fileNames) {
        List<ImageUploadResponse> responses = fileNames.stream()
                .map(fileName -> prepareUploadInternal(fileName, S3_CHATS_DIR))
                .toList();
        log.debug("Presigned URL 생성 완료{} 파일 개수: {}", S3_CHATS_DIR, responses.size());
        return responses;
    }

    private ImageUploadResponse prepareUploadInternal(String fileName, String directory) {
        validateFileExtension(fileName);
        String finalFileName = generateFileName(fileName);
        String key = directory + finalFileName;
        return presignPut(key);
    }

    public String getS3Url(String key) {
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(s3Properties.getBucket(), s3Properties.getRegion(), key);
    }

    private ImageUploadResponse presignPut(String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_URL_TTL)
                .putObjectRequest(putObjectRequest)
                .build();

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        String fileUrl = getS3Url(key);
        return new ImageUploadResponse(presignedUrl, fileUrl);
    }

    private void validateFileExtension(String fileName) {
        String extension = StringUtils.getFilenameExtension(fileName);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw InvalidFileExtensionException.invalidExtension(ALLOWED_EXTENSIONS);
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        return UUID.randomUUID() + "." + extension;
    }
}

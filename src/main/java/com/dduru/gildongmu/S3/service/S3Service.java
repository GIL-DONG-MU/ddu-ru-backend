package com.dduru.gildongmu.S3.service;

import com.dduru.gildongmu.S3.dto.ImageUploadResponse;
import com.dduru.gildongmu.config.S3Properties;
import com.dduru.gildongmu.S3.exception.InvalidFileExtensionException;
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
   /* private static final String S3_SURVEY_DIR = "survey/";*/
    private static final Duration PRESIGNED_URL_TTL = Duration.ofMinutes(10);

    public ImageUploadResponse preparePostImageUpload(String fileName) {
        log.debug("Presigned URL 생성 시작(posts) - fileName: {}", fileName);
        ImageUploadResponse response = prepareUploadInternal(fileName, S3_POSTS_DIR, true);
        log.info("Presigned URL 생성 완료(posts) - fileName: {}", fileName);
        return response;
    }

   /* public ImageUploadResponse prepareSurveyImageUpload(String fileName) {
        log.debug("Presigned URL 생성 시작(survey) - fileName: {}", fileName);
        ImageUploadResponse response = prepareUploadInternal(fileName, S3_SURVEY_DIR, false);
        log.info("Presigned URL 생성 완료(survey) - fileName: {}", fileName);
        return response;
    }*/

    private ImageUploadResponse prepareUploadInternal(String fileName, String directory, boolean useUuid) {
        validateFileExtension(fileName);
        String finalFileName = useUuid ? generateFileName(fileName) : fileName;
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
            throw new InvalidFileExtensionException("허용되지 않는 파일 확장자입니다. 허용 확장자: " + ALLOWED_EXTENSIONS);
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        return UUID.randomUUID().toString() + "." + extension;
    }
}

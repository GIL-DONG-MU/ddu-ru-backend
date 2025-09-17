package com.dduru.gildongmu.S3.service;

import com.dduru.gildongmu.S3.dto.ImageUploadResponse;
import com.dduru.gildongmu.config.S3Properties;
import com.dduru.gildongmu.S3.exception.InvalidFileExtensionException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final String S3_POSTS_DIR = "posts/";

    public ImageUploadResponse prepareUpload(String fileName) {
        validateFileExtension(fileName);

        String uniqueFileName = generateFileName(fileName);
        String key = S3_POSTS_DIR + uniqueFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        String fileUrl = "https://%s.s3.%s.amazonaws.com/%s".formatted(s3Properties.getBucket(), s3Properties.getRegion(), key);

        return new ImageUploadResponse(presignedUrl, fileUrl);
    }

    private void validateFileExtension(String fileName) {
        String extension = StringUtils.getFilenameExtension(fileName);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new InvalidFileExtensionException(
                "허용되지 않는 파일 확장자입니다. 허용 확장자: " + ALLOWED_EXTENSIONS);
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        return UUID.randomUUID() + "." + extension;
    }
}

package com.dduru.gildongmu.S3.service;

import com.dduru.gildongmu.S3.exception.EmptyFileException;
import com.dduru.gildongmu.S3.exception.FileUploadFailedException;
import com.dduru.gildongmu.S3.exception.InvalidFileExtensionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final String S3_POSTS_DIR = "posts/";

    public String uploadFile(MultipartFile file) {
        validateFile(file);

        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String key = S3_POSTS_DIR + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return getFileUrl(key);
        } catch (IOException | SdkException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            throw new FileUploadFailedException();
        }
    }

    public List<String> uploadFiles(List<MultipartFile> files) {
        return files.stream()
                .map(this::uploadFile)
                .collect(Collectors.toList());
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException();
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new InvalidFileExtensionException(
                "허용되지 않는 파일 확장자입니다. 허용 확장자: " + ALLOWED_EXTENSIONS);
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        return UUID.randomUUID().toString() + "." + extension;
    }

    private String getFileUrl(String key) {
        GetUrlRequest request = GetUrlRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        return s3Client.utilities().getUrl(request).toString();
    }
}

package com.dduru.gildongmu.chat.validation;

import com.dduru.gildongmu.common.config.S3Properties;
import com.dduru.gildongmu.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("채팅 이미지 URL validator 테스트")
class ChatImageUrlValidatorTest {

    private ChatImageUrlValidator validator;

    @BeforeEach
    void setUp() {
        S3Properties s3Properties = new S3Properties();
        s3Properties.setBucket("dummy-bucket");
        s3Properties.setRegion("ap-northeast-2");
        validator = new ChatImageUrlValidator(s3Properties);
    }

    @Test
    @DisplayName("우리 S3 chats 경로 URL이면 허용한다")
    void validateAndNormalize_validChatS3Url_returnsUrl() {
        String url = "https://dummy-bucket.s3.ap-northeast-2.amazonaws.com/chats/123e4567-e89b-12d3-a456-426614174000.JPG";

        String result = validator.validateAndNormalize(url);

        assertThat(result).isEqualTo(url);
    }

    @Test
    @DisplayName("외부 호스트 URL이면 거부한다")
    void validateAndNormalize_externalHost_throwsNotAllowed() {
        assertThatThrownBy(() -> validator.validateAndNormalize("https://example.com/chats/test.jpg"))
                .hasMessage(ErrorCode.CHAT_IMAGE_URL_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("S3 URL여도 chats 경로가 아니면 거부한다")
    void validateAndNormalize_nonChatsPath_throwsNotAllowed() {
        assertThatThrownBy(() -> validator.validateAndNormalize(
                "https://dummy-bucket.s3.ap-northeast-2.amazonaws.com/profiles/test.jpg"))
                .hasMessage(ErrorCode.CHAT_IMAGE_URL_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("presigned URL처럼 query가 포함되면 거부한다")
    void validateAndNormalize_presignedUrlWithQuery_throwsNotAllowed() {
        assertThatThrownBy(() -> validator.validateAndNormalize(
                "https://dummy-bucket.s3.ap-northeast-2.amazonaws.com/chats/test.jpg?X-Amz-Signature=abc"))
                .hasMessage(ErrorCode.CHAT_IMAGE_URL_NOT_ALLOWED.getMessage());
    }
}

CREATE TABLE user_fcm_tokens
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(500) NOT NULL,
    device_type VARCHAR(10)  NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    modified_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_token (token),
    UNIQUE KEY uk_user_device (user_id, device_type),
    CONSTRAINT fk_fcm_token_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

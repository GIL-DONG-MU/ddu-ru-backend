CREATE TABLE IF NOT EXISTS notifications
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_user_id BIGINT       NOT NULL,
    type              VARCHAR(50)  NOT NULL,
    body              VARCHAR(500) NOT NULL,
    resource_type     VARCHAR(50)  NOT NULL,
    resource_id       BIGINT       NOT NULL,
    is_read           TINYINT(1)   NOT NULL DEFAULT 0,
    created_at        DATETIME(6)  NOT NULL,
    modified_at       DATETIME(6)  NULL,
    read_at           DATETIME(6)  NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (recipient_user_id) REFERENCES users (id),
    INDEX idx_notifications_recipient (recipient_user_id, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

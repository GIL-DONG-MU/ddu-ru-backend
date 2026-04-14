-- 슈퍼호스트 1차 MVP: 티켓 지급/사용 + 게시글 상위 노출 (온보딩 소스당 1회)

CREATE TABLE super_host_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    source VARCHAR(32) NOT NULL COMMENT 'ONBOARDING_SURVEY',
    status VARCHAR(32) NOT NULL COMMENT 'UNUSED, USED',
    boost_duration_days INT NOT NULL,
    used_at DATETIME(6) NULL,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    CONSTRAINT fk_super_host_tickets_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_super_host_tickets_user_source UNIQUE (user_id, source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_super_host_tickets_user_status
    ON super_host_tickets (user_id, status);

CREATE TABLE super_host_exposures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    ticket_id BIGINT NOT NULL,
    started_at DATETIME(6) NOT NULL,
    ended_at DATETIME(6) NOT NULL,
    status VARCHAR(32) NOT NULL COMMENT 'ACTIVE, ENDED, CANCELLED',
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    CONSTRAINT fk_super_host_exposures_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_super_host_exposures_post
        FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_super_host_exposures_ticket
        FOREIGN KEY (ticket_id) REFERENCES super_host_tickets (id) ON DELETE CASCADE,
    CONSTRAINT uk_super_host_exposures_ticket UNIQUE (ticket_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_super_host_exposures_status_ended_at
    ON super_host_exposures (status, ended_at);

CREATE INDEX idx_super_host_exposures_post_status
    ON super_host_exposures (post_id, status);

CREATE INDEX idx_super_host_exposures_user_status
    ON super_host_exposures (user_id, status);

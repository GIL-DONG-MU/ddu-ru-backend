CREATE TABLE user_recommendation_destination_preferences
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    user_id        BIGINT      NOT NULL,
    preference_type VARCHAR(20) NOT NULL,
    country_code   VARCHAR(2)  NULL,
    destination_id BIGINT      NULL,
    created_at     DATETIME(6) NOT NULL,
    modified_at    DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_recommendation_destination_country (user_id, preference_type, country_code),
    UNIQUE KEY uk_recommendation_destination_city (user_id, preference_type, destination_id),
    KEY idx_recommendation_destination_user_type (user_id, preference_type),
    KEY idx_recommendation_destination_country (country_code),
    KEY idx_recommendation_destination_destination (destination_id),
    CONSTRAINT fk_recommendation_destination_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_recommendation_destination_destination FOREIGN KEY (destination_id) REFERENCES destinations (id) ON DELETE CASCADE,
    CONSTRAINT chk_recommendation_destination_type CHECK (preference_type IN ('COUNTRY', 'CITY')),
    CONSTRAINT chk_recommendation_destination_value CHECK (
        (preference_type = 'COUNTRY' AND country_code IS NOT NULL AND destination_id IS NULL)
            OR (preference_type = 'CITY' AND country_code IS NULL AND destination_id IS NOT NULL)
    )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE user_recommendation_available_dates
(
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    start_date  DATE        NOT NULL,
    end_date    DATE        NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    modified_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_recommendation_available_dates_user_range (user_id, start_date, end_date),
    KEY idx_recommendation_available_dates_user_start_end (user_id, start_date, end_date),
    CONSTRAINT fk_recommendation_available_dates_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_recommendation_available_dates_range CHECK (start_date <= end_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE mate_recommendation_batches
(
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    user_id             BIGINT      NOT NULL,
    recommendation_date DATE        NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    failure_reason      VARCHAR(500) NULL,
    created_at          DATETIME(6) NOT NULL,
    modified_at         DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_mate_recommendation_batches_user_date (user_id, recommendation_date),
    KEY idx_mate_recommendation_batches_date_status (recommendation_date, status),
    CONSTRAINT fk_mate_recommendation_batches_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_mate_recommendation_batches_status CHECK (status IN ('CREATED', 'COMPLETED', 'EMPTY', 'FAILED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE mate_recommendations
(
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    batch_id         BIGINT      NOT NULL,
    user_id          BIGINT      NOT NULL,
    post_id          BIGINT      NOT NULL,
    rank_order       INT         NOT NULL,
    match_percentage INT         NOT NULL,
    match_reasons    JSON        NOT NULL,
    caution_points   JSON        NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at       DATETIME(6) NOT NULL,
    modified_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_mate_recommendations_batch_rank (batch_id, rank_order),
    UNIQUE KEY uk_mate_recommendations_batch_post (batch_id, post_id),
    KEY idx_mate_recommendations_user_post (user_id, post_id),
    KEY idx_mate_recommendations_batch_status (batch_id, status),
    CONSTRAINT fk_mate_recommendations_batch FOREIGN KEY (batch_id) REFERENCES mate_recommendation_batches (id) ON DELETE CASCADE,
    CONSTRAINT fk_mate_recommendations_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_mate_recommendations_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT chk_mate_recommendations_rank CHECK (rank_order BETWEEN 1 AND 3),
    CONSTRAINT chk_mate_recommendations_match CHECK (match_percentage BETWEEN 0 AND 100),
    CONSTRAINT chk_mate_recommendations_status CHECK (status IN ('ACTIVE', 'APPLIED', 'PASSED')),
    CONSTRAINT chk_mate_recommendations_match_reasons_json CHECK (JSON_VALID(match_reasons)),
    CONSTRAINT chk_mate_recommendations_caution_points_json CHECK (JSON_VALID(caution_points))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE mate_recommendation_passes
(
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    user_id           BIGINT      NOT NULL,
    post_id           BIGINT      NOT NULL,
    recommendation_id BIGINT      NULL,
    created_at        DATETIME(6) NOT NULL,
    modified_at       DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_mate_recommendation_passes_user_post (user_id, post_id),
    KEY idx_mate_recommendation_passes_user (user_id),
    KEY idx_mate_recommendation_passes_post (post_id),
    CONSTRAINT fk_mate_recommendation_passes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_mate_recommendation_passes_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_mate_recommendation_passes_recommendation FOREIGN KEY (recommendation_id) REFERENCES mate_recommendations (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

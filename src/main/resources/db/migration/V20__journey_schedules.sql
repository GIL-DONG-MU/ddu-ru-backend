CREATE TABLE journey_schedules
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    journey_id BIGINT       NOT NULL,
    title      VARCHAR(30)  NOT NULL,
    category   VARCHAR(50)  NULL,
    day_offset INT          NOT NULL,
    start_time TIME         NULL,
    end_time   TIME         NULL,
    place_name VARCHAR(30)  NOT NULL,
    memo       VARCHAR(100) NULL,
    image_url  TEXT         NULL,
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6)  NULL,
    deleted_by BIGINT       NULL,
    created_at DATETIME(6)  NULL,
    modified_at DATETIME(6) NULL,
    CONSTRAINT fk_journey_schedules_journey FOREIGN KEY (journey_id) REFERENCES journeys (id) ON DELETE CASCADE
);

CREATE INDEX idx_journey_schedules_journey_deleted_offset_time
    ON journey_schedules (journey_id, is_deleted, day_offset, start_time);

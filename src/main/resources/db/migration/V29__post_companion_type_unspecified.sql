UPDATE posts
SET companion_type = 'UNSPECIFIED'
WHERE companion_type IS NULL;

ALTER TABLE posts
    MODIFY companion_type VARCHAR(255) NOT NULL;

CREATE TABLE IF NOT EXISTS series_dict (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    alias VARCHAR(50),
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sort_weight INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS media_resource (
    id BIGINT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    alias VARCHAR(100),
    series_id BIGINT NOT NULL,
    age_range VARCHAR(20) NOT NULL,
    media_type VARCHAR(20) NOT NULL,
    play_url VARCHAR(500) NOT NULL,
    cover_url VARCHAR(500) NOT NULL,
    description VARCHAR(500),
    source_type VARCHAR(30),
    source_platform VARCHAR(50),
    source_remark VARCHAR(200),
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    is_abnormal BOOLEAN NOT NULL DEFAULT FALSE,
    abnormal_remark VARCHAR(200),
    click_count BIGINT NOT NULL DEFAULT 0,
    rating_avg NUMERIC(4, 2) NOT NULL DEFAULT 0,
    rating_count INTEGER NOT NULL DEFAULT 0,
    sort_weight INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS media_rating (
    id BIGINT PRIMARY KEY,
    media_id BIGINT NOT NULL,
    score SMALLINT NOT NULL,
    device_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_media_resource_filter
    ON media_resource (is_published, is_abnormal, media_type, age_range, series_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_media_resource_click ON media_resource (click_count DESC);
CREATE INDEX IF NOT EXISTS idx_media_resource_rating ON media_resource (rating_avg DESC);
CREATE INDEX IF NOT EXISTS idx_media_rating_media_id ON media_rating (media_id);
CREATE INDEX IF NOT EXISTS idx_series_dict_enabled ON series_dict (is_enabled, sort_weight);

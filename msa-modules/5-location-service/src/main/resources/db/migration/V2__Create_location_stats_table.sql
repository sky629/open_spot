-- Create location_stats table for tracking location statistics

CREATE TABLE location.location_stats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    location_id UUID NOT NULL,
    stats_date DATE NOT NULL,
    stats_type VARCHAR(20) NOT NULL CHECK (stats_type IN ('DAILY', 'MONTHLY', 'YEARLY')),
    view_count BIGINT NOT NULL DEFAULT 0,
    visit_count BIGINT NOT NULL DEFAULT 0,
    review_count BIGINT NOT NULL DEFAULT 0,
    average_rating DECIMAL(3,2),
    favorite_count BIGINT NOT NULL DEFAULT 0,
    unique_visitor_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Unique constraint to prevent duplicate stats for same location/date/type
    CONSTRAINT uk_location_stats_location_date_type UNIQUE (location_id, stats_date, stats_type),

    -- Check constraints for non-negative values
    CONSTRAINT ck_location_stats_view_count CHECK (view_count >= 0),
    CONSTRAINT ck_location_stats_visit_count CHECK (visit_count >= 0),
    CONSTRAINT ck_location_stats_review_count CHECK (review_count >= 0),
    CONSTRAINT ck_location_stats_favorite_count CHECK (favorite_count >= 0),
    CONSTRAINT ck_location_stats_unique_visitor_count CHECK (unique_visitor_count >= 0),
    CONSTRAINT ck_location_stats_average_rating CHECK (average_rating IS NULL OR (average_rating >= 1.0 AND average_rating <= 5.0))
);

-- Create indexes for better query performance
CREATE INDEX idx_location_stats_location_id ON location.location_stats(location_id);
CREATE INDEX idx_location_stats_date ON location.location_stats(stats_date);
CREATE INDEX idx_location_stats_type ON location.location_stats(stats_type);
CREATE INDEX idx_location_stats_location_type ON location.location_stats(location_id, stats_type);

-- Add comments for documentation
COMMENT ON TABLE location.location_stats IS 'Stores statistical information for locations (daily, monthly, yearly aggregations)';
COMMENT ON COLUMN location.location_stats.location_id IS 'Reference to the location';
COMMENT ON COLUMN location.location_stats.stats_date IS 'Date for the statistics (first day of period for monthly/yearly)';
COMMENT ON COLUMN location.location_stats.stats_type IS 'Type of statistics: DAILY, MONTHLY, or YEARLY';
COMMENT ON COLUMN location.location_stats.view_count IS 'Number of views for the location in this period';
COMMENT ON COLUMN location.location_stats.visit_count IS 'Number of visits recorded for this period';
COMMENT ON COLUMN location.location_stats.review_count IS 'Number of reviews written in this period';
COMMENT ON COLUMN location.location_stats.average_rating IS 'Average rating for this period (1.0 to 5.0)';
COMMENT ON COLUMN location.location_stats.favorite_count IS 'Number of times favorited in this period';
COMMENT ON COLUMN location.location_stats.unique_visitor_count IS 'Number of unique visitors in this period';
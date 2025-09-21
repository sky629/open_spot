-- Create remaining tables for location service

-- Create locations table
CREATE TABLE location.locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    address VARCHAR(200),
    category VARCHAR(50) NOT NULL,
    coordinates GEOMETRY(POINT, 4326) NOT NULL,
    created_by UUID NOT NULL,
    phone_number VARCHAR(20),
    website_url VARCHAR(500),
    business_hours VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    view_count BIGINT NOT NULL DEFAULT 0,
    average_rating DECIMAL(3,2),
    review_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Check constraints
    CONSTRAINT ck_locations_name_not_empty CHECK (TRIM(name) != ''),
    CONSTRAINT ck_locations_view_count CHECK (view_count >= 0),
    CONSTRAINT ck_locations_review_count CHECK (review_count >= 0),
    CONSTRAINT ck_locations_average_rating CHECK (average_rating IS NULL OR (average_rating >= 1.0 AND average_rating <= 5.0))
);

-- Create location_visits table
CREATE TABLE location.location_visits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    location_id UUID NOT NULL,
    user_id UUID NOT NULL,
    visited_at TIMESTAMP WITH TIME ZONE NOT NULL,
    memo VARCHAR(500),
    visit_duration_minutes INTEGER,
    companion_count INTEGER,
    visit_purpose VARCHAR(20),
    is_favorite BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Check constraints
    CONSTRAINT ck_location_visits_visit_duration CHECK (visit_duration_minutes IS NULL OR visit_duration_minutes BETWEEN 1 AND 1440),
    CONSTRAINT ck_location_visits_companion_count CHECK (companion_count IS NULL OR companion_count BETWEEN 0 AND 100),
    CONSTRAINT ck_location_visits_visit_purpose CHECK (visit_purpose IS NULL OR visit_purpose IN ('BUSINESS', 'LEISURE', 'DATING', 'FAMILY', 'FRIENDS', 'SOLO', 'EXERCISE', 'STUDY', 'SHOPPING', 'DINING', 'OTHER'))
);

-- Create reviews table
CREATE TABLE location.reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    location_id UUID NOT NULL,
    user_id UUID NOT NULL,
    rating DECIMAL(2,1) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    image_urls TEXT[], -- PostgreSQL array for storing multiple image URLs
    helpful_count BIGINT NOT NULL DEFAULT 0,
    reported_count BIGINT NOT NULL DEFAULT 0,
    visited_date DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Check constraints
    CONSTRAINT ck_reviews_rating CHECK (rating >= 1.0 AND rating <= 5.0),
    CONSTRAINT ck_reviews_content_not_empty CHECK (TRIM(content) != ''),
    CONSTRAINT ck_reviews_content_length CHECK (LENGTH(content) <= 2000),
    CONSTRAINT ck_reviews_helpful_count CHECK (helpful_count >= 0),
    CONSTRAINT ck_reviews_reported_count CHECK (reported_count >= 0),
    CONSTRAINT ck_reviews_status CHECK (status IN ('ACTIVE', 'HIDDEN', 'DELETED')),

    -- Unique constraint for one review per user per location
    CONSTRAINT uk_reviews_location_user UNIQUE (location_id, user_id)
);

-- Create indexes for better query performance

-- Locations indexes
CREATE INDEX idx_locations_category ON location.locations(category);
CREATE INDEX idx_locations_created_by ON location.locations(created_by);
CREATE INDEX idx_locations_is_active ON location.locations(is_active);
CREATE INDEX idx_locations_view_count ON location.locations(view_count DESC);
CREATE INDEX idx_locations_average_rating ON location.locations(average_rating DESC) WHERE average_rating IS NOT NULL;
CREATE INDEX idx_locations_coordinates ON location.locations USING GIST(coordinates);
CREATE INDEX idx_locations_created_at ON location.locations(created_at DESC);

-- Location visits indexes
CREATE INDEX idx_location_visits_location_user ON location.location_visits(location_id, user_id);
CREATE INDEX idx_location_visits_user ON location.location_visits(user_id);
CREATE INDEX idx_location_visits_visited_at ON location.location_visits(visited_at);
CREATE INDEX idx_location_visits_is_favorite ON location.location_visits(user_id, is_favorite) WHERE is_favorite = true;
CREATE INDEX idx_location_visits_location_visited_at ON location.location_visits(location_id, visited_at DESC);

-- Reviews indexes
CREATE INDEX idx_reviews_location ON location.reviews(location_id);
CREATE INDEX idx_reviews_user ON location.reviews(user_id);
CREATE INDEX idx_reviews_status ON location.reviews(status);
CREATE INDEX idx_reviews_location_status ON location.reviews(location_id, status);
CREATE INDEX idx_reviews_rating ON location.reviews(rating DESC);
CREATE INDEX idx_reviews_helpful_count ON location.reviews(helpful_count DESC);
CREATE INDEX idx_reviews_created_at ON location.reviews(created_at DESC);

-- Add comments for documentation
COMMENT ON TABLE location.locations IS 'Stores location information created by users';
COMMENT ON TABLE location.location_visits IS 'Records user visits to locations with optional details';
COMMENT ON TABLE location.reviews IS 'User reviews and ratings for locations';

COMMENT ON COLUMN location.locations.coordinates IS 'Geographic coordinates in WGS84 (EPSG:4326) format';
COMMENT ON COLUMN location.locations.category IS 'Location category (RESTAURANT, CAFE, SHOPPING, etc.)';
COMMENT ON COLUMN location.location_visits.visit_purpose IS 'Purpose of the visit (BUSINESS, LEISURE, etc.)';
COMMENT ON COLUMN location.reviews.image_urls IS 'Array of image URLs associated with the review';
COMMENT ON COLUMN location.reviews.status IS 'Review status: ACTIVE, HIDDEN, or DELETED';
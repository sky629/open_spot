-- Add individual latitude and longitude columns for JPA entity compatibility

-- Add latitude and longitude columns to locations table
ALTER TABLE location.locations
ADD COLUMN latitude DOUBLE PRECISION,
ADD COLUMN longitude DOUBLE PRECISION;

-- Extract latitude and longitude from coordinates geometry and update the new columns
UPDATE location.locations
SET
    latitude = ST_Y(coordinates),
    longitude = ST_X(coordinates)
WHERE coordinates IS NOT NULL;

-- Make the new columns NOT NULL after populating them
ALTER TABLE location.locations
ALTER COLUMN latitude SET NOT NULL,
ALTER COLUMN longitude SET NOT NULL;

-- Add check constraints for valid coordinate ranges
ALTER TABLE location.locations
ADD CONSTRAINT ck_locations_latitude CHECK (latitude >= -90.0 AND latitude <= 90.0),
ADD CONSTRAINT ck_locations_longitude CHECK (longitude >= -180.0 AND longitude <= 180.0);

-- Create indexes for latitude/longitude queries
CREATE INDEX idx_locations_latitude ON location.locations(latitude);
CREATE INDEX idx_locations_longitude ON location.locations(longitude);
CREATE INDEX idx_locations_lat_lng ON location.locations(latitude, longitude);

-- Add comments
COMMENT ON COLUMN location.locations.latitude IS 'Latitude coordinate in WGS84 decimal degrees';
COMMENT ON COLUMN location.locations.longitude IS 'Longitude coordinate in WGS84 decimal degrees';

-- Note: We keep both the coordinates GEOMETRY column for spatial queries
-- and individual lat/lng columns for JPA entity compatibility
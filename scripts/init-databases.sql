-- Create databases for each service
CREATE DATABASE openspot;

-- Connect to openspot_analysis database and enable PostGIS extension
\c openspot;
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE openspot TO openspot;

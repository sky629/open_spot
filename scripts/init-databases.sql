-- Create databases for each service
CREATE DATABASE openspot;

-- Connect to openspot database and enable PostGIS extension
\c openspot;
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

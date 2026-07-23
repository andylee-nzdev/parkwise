-- V1: core schema for parkwise (PostGIS)
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE car_park (
    id                BIGINT PRIMARY KEY,          -- AT OBJECTID
    name              TEXT,
    description       TEXT,
    data_type         TEXT,                        -- Open Air / Covered / Motorcycle / Disabled
    street            TEXT,
    suburb            TEXT,
    city              TEXT,
    total_spaces      INTEGER,
    mobility_spaces   INTEGER,
    motorcycle_spaces INTEGER,
    available_spaces  INTEGER,                     -- live for only 2 sites today (nullable)
    clearance_m       TEXT,
    geom              geometry(Point, 4326) NOT NULL,
    ingested_at       TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_car_park_geom ON car_park USING GIST (geom);
CREATE INDEX idx_car_park_type ON car_park (data_type);

CREATE TABLE park_and_ride (
    id        BIGINT PRIMARY KEY,
    name      TEXT,
    stop_type TEXT,                                -- Bus / Train / Ferry
    num_bays  INTEGER,
    status    TEXT,
    geom      geometry(Point, 4326) NOT NULL
);
CREATE INDEX idx_pnr_geom ON park_and_ride USING GIST (geom);

-- Self-built occupancy history (Phase 3 ML feature store)
CREATE TABLE occupancy_reading (
    car_park_id      BIGINT REFERENCES car_park(id),
    observed_at      TIMESTAMPTZ NOT NULL,
    available_spaces INTEGER,
    PRIMARY KEY (car_park_id, observed_at)
);

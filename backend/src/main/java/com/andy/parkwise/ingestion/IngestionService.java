package com.andy.parkwise.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pulls AT parking layers and upserts them into PostGIS.
 * Runs on startup (once, if empty) and on the configured cron.
 * Kept deliberately explicit — each AT field is mapped by hand so schema drift is visible.
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final AtParkingClient client;
    private final JdbcClient jdbc;

    public IngestionService(AtParkingClient client, JdbcClient jdbc) {
        this.client = client;
        this.jdbc = jdbc;
    }

    @Scheduled(cron = "${at.parking.ingest-cron}")
    @Transactional
    public void ingestAll() {
        int cp = ingestCarParks();
        int pnr = ingestParkAndRide();
        log.info("Ingestion complete: {} car parks, {} park & ride sites", cp, pnr);
    }

    int ingestCarParks() {
        JsonNode fc = client.fetchCarParks();
        int count = 0;
        for (JsonNode f : fc.path("features")) {
            JsonNode p = f.path("properties");
            JsonNode coords = f.path("geometry").path("coordinates");
            double lon = coords.path(0).asDouble();
            double lat = coords.path(1).asDouble();
            jdbc.sql("""
                INSERT INTO car_park (id, name, description, data_type, street, suburb, city,
                    total_spaces, mobility_spaces, motorcycle_spaces, available_spaces, clearance_m, geom)
                VALUES (:id, :name, :desc, :type, :street, :suburb, :city,
                    :total, :mob, :moto, :avail, :clear,
                    ST_SetSRID(ST_MakePoint(:lon, :lat), 4326))
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name, description = EXCLUDED.description, data_type = EXCLUDED.data_type,
                    total_spaces = EXCLUDED.total_spaces, available_spaces = EXCLUDED.available_spaces,
                    geom = EXCLUDED.geom, ingested_at = now()
                """)
                .param("id", p.path("OBJECTID").asLong())
                .param("name", text(p, "SHORTDESCRIPTION"))
                .param("desc", text(p, "LONGDESCRIPTION"))
                .param("type", text(p, "DATATYPE"))
                .param("street", text(p, "STREET"))
                .param("suburb", text(p, "SUBURB"))
                .param("city", text(p, "CITY"))
                .param("total", intOrNull(p, "TOTALSPACES"))
                .param("mob", intOrNull(p, "MOBILITYSPACES"))
                .param("moto", intOrNull(p, "MOTORCYCLESPACES"))
                .param("avail", intOrNull(p, "AVAILABLESPACES"))
                .param("clear", text(p, "CLEARANCEMETERS"))
                .param("lon", lon).param("lat", lat)
                .update();
            count++;
        }
        return count;
    }

    int ingestParkAndRide() {
        JsonNode fc = client.fetchParkAndRide();
        int count = 0;
        for (JsonNode f : fc.path("features")) {
            JsonNode p = f.path("properties");
            JsonNode coords = f.path("geometry").path("coordinates");
            jdbc.sql("""
                INSERT INTO park_and_ride (id, name, stop_type, num_bays, status, geom)
                VALUES (:id, :name, :type, :bays, :status,
                    ST_SetSRID(ST_MakePoint(:lon, :lat), 4326))
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name, stop_type = EXCLUDED.stop_type,
                    num_bays = EXCLUDED.num_bays, geom = EXCLUDED.geom
                """)
                .param("id", p.path("OBJECTID").asLong())
                .param("name", text(p, "STOPNAME"))
                .param("type", text(p, "STOPTYPE"))
                .param("bays", intOrNull(p, "NUMOFBAY"))
                .param("status", text(p, "RECORDSTATUS"))
                .param("lon", coords.path(0).asDouble())
                .param("lat", coords.path(1).asDouble())
                .update();
            count++;
        }
        return count;
    }

    private static String text(JsonNode p, String field) {
        JsonNode n = p.get(field);
        return (n == null || n.isNull()) ? null : n.asText();
    }

    private static Integer intOrNull(JsonNode p, String field) {
        JsonNode n = p.get(field);
        if (n == null || n.isNull() || n.asText().isBlank()) return null;
        try { return (int) Math.round(Double.parseDouble(n.asText())); }
        catch (NumberFormatException e) { return null; }
    }
}

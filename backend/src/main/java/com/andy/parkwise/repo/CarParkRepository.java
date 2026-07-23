package com.andy.parkwise.repo;

import com.andy.parkwise.domain.CarPark;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PostGIS-backed access. The radius query is the heart of the recommender:
 * ST_DWithin over a GiST index + geography distance for correct metres.
 */
@Repository
public class CarParkRepository {

    private final JdbcClient jdbc;

    public CarParkRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Real car-park facilities within {@code radiusM} of (lon,lat), nearest first. */
    public List<CarParkDistance> findWithinRadius(double lon, double lat, double radiusM, int limit) {
        String sql = """
            SELECT id, name, description, data_type, street, suburb, city,
                   total_spaces, mobility_spaces, motorcycle_spaces, available_spaces, clearance_m,
                   ST_X(geom) AS lon, ST_Y(geom) AS lat,
                   ST_Distance(geom::geography,
                               ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography) AS dist_m
            FROM car_park
            WHERE data_type IN ('Open Air Parking', 'Covered Parking')
              AND ST_DWithin(geom::geography,
                             ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radius)
            ORDER BY dist_m
            LIMIT :limit
            """;
        return jdbc.sql(sql)
                .param("lon", lon)
                .param("lat", lat)
                .param("radius", radiusM)
                .param("limit", limit)
                .query((rs, n) -> new CarParkDistance(
                        new CarPark(
                                rs.getLong("id"), rs.getString("name"), rs.getString("description"),
                                rs.getString("data_type"), rs.getString("street"), rs.getString("suburb"),
                                rs.getString("city"), (Integer) rs.getObject("total_spaces"),
                                (Integer) rs.getObject("mobility_spaces"), (Integer) rs.getObject("motorcycle_spaces"),
                                (Integer) rs.getObject("available_spaces"), rs.getString("clearance_m"),
                                rs.getDouble("lon"), rs.getDouble("lat")),
                        rs.getDouble("dist_m")))
                .list();
    }

    public record CarParkDistance(CarPark carPark, double distanceMeters) {}
}

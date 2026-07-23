package com.andy.parkwise.domain;

/**
 * A ranked recommendation returned to the client.
 * {@code availabilitySource} is honest about where the number came from:
 * LIVE (AT feed), ESTIMATED (model), or UNKNOWN.
 */
public record Recommendation(
        long id,
        String name,
        String dataType,
        Integer totalSpaces,
        Integer availableSpaces,
        AvailabilitySource availabilitySource,
        long distanceMeters,
        double walkMinutes,
        double score,
        double lon,
        double lat
) {
    public enum AvailabilitySource { LIVE, ESTIMATED, UNKNOWN }
}

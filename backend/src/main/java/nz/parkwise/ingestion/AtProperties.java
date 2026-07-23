package nz.parkwise.ingestion;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds the {@code at.parking.*} config. */
@ConfigurationProperties(prefix = "at.parking")
public record AtProperties(
        String baseUrl,
        int carParkLayer,
        int parkAndRideLayer,
        String ingestCron
) {}

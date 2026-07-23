package com.andy.parkwise.occupancy;

import com.andy.parkwise.domain.CarPark;
import com.andy.parkwise.domain.Recommendation.AvailabilitySource;

import java.util.Optional;

/**
 * Strategy interface for "how many spaces are free right now?".
 *
 * AT publishes NO network-wide real-time occupancy API, so this abstraction lets us
 * swap implementations without touching the recommender:
 *   - {@link StaticOccupancyProvider}    — trust AT's AVAILABLESPACES where present (LIVE), else UNKNOWN
 *   - {@link SimulatedOccupancyProvider} — plausible synthetic feed for demos
 *   - (Phase 3) a model-backed provider  — ESTIMATED from historical occupancy_reading data
 *
 * If AT ever ships a real API, that's a one-class change.
 */
public interface OccupancyProvider {

    /** @return available spaces for the car park, or empty if genuinely unknown. */
    Optional<Integer> availableSpaces(CarPark carPark);

    /** Where the number comes from — surfaced to the client for honesty. */
    AvailabilitySource source();
}

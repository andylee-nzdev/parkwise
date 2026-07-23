package com.andy.parkwise.domain;

/**
 * A car-park facility from Auckland Transport open data.
 * {@code availableSpaces} is null for all but a couple of sites AT feeds live —
 * the whole occupancy design exists to fill that gap (see OccupancyProvider).
 */
public record CarPark(
        long id,
        String name,
        String description,
        String dataType,
        String street,
        String suburb,
        String city,
        Integer totalSpaces,
        Integer mobilitySpaces,
        Integer motorcycleSpaces,
        Integer availableSpaces,
        String clearanceM,
        double lon,
        double lat
) {}

package nz.parkwise.occupancy;

import nz.parkwise.domain.CarPark;
import nz.parkwise.domain.Recommendation.AvailabilitySource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Default provider: use AT's AVAILABLESPACES where AT actually feeds it (LIVE),
 * otherwise report UNKNOWN. Honest, no invention.
 */
@Component
@ConditionalOnProperty(name = "occupancy.provider", havingValue = "static", matchIfMissing = true)
public class StaticOccupancyProvider implements OccupancyProvider {

    @Override
    public Optional<Integer> availableSpaces(CarPark carPark) {
        return Optional.ofNullable(carPark.availableSpaces());
    }

    @Override
    public AvailabilitySource source() {
        return AvailabilitySource.LIVE;
    }
}

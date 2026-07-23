package nz.parkwise.occupancy;

import nz.parkwise.domain.CarPark;
import nz.parkwise.domain.Recommendation.AvailabilitySource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Demo provider: generates a plausible occupancy from total spaces + time of day,
 * so the full UI can be demonstrated where real live data is absent.
 * Clearly labelled ESTIMATED so it is never passed off as real.
 *
 * Enable with: occupancy.provider=simulated
 */
@Component
@ConditionalOnProperty(name = "occupancy.provider", havingValue = "simulated")
public class SimulatedOccupancyProvider implements OccupancyProvider {

    @Override
    public Optional<Integer> availableSpaces(CarPark carPark) {
        Integer total = carPark.totalSpaces();
        if (total == null || total <= 0) return Optional.empty();
        int hour = LocalTime.now().getHour();
        // busier 8-10 and 16-18; free-ish overnight
        double occupancy = switch (hour / 3) {
            case 2, 3 -> 0.75;  // 06:00-11:59
            case 4, 5 -> 0.70;  // 12:00-17:59
            case 6    -> 0.55;  // 18:00-20:59
            default   -> 0.25;
        };
        occupancy += ThreadLocalRandom.current().nextDouble(-0.1, 0.1);
        occupancy = Math.max(0, Math.min(1, occupancy));
        return Optional.of((int) Math.round(total * (1 - occupancy)));
    }

    @Override
    public AvailabilitySource source() {
        return AvailabilitySource.ESTIMATED;
    }
}

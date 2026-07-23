package nz.parkwise.recommend;

import nz.parkwise.domain.CarPark;
import nz.parkwise.domain.Recommendation;
import nz.parkwise.domain.Recommendation.AvailabilitySource;
import nz.parkwise.occupancy.OccupancyProvider;
import nz.parkwise.repo.CarParkRepository;
import nz.parkwise.repo.CarParkRepository.CarParkDistance;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Pure unit test — no DB. Verifies the ranking logic:
 * a closer, larger car park should outrank a far, small one.
 */
class RecommendationServiceTest {

    @Test
    void closerLargerParkRanksHigher() {
        CarParkRepository repo = mock(CarParkRepository.class);
        OccupancyProvider occ = mock(OccupancyProvider.class);
        when(occ.availableSpaces(any())).thenReturn(Optional.empty());
        when(occ.source()).thenReturn(AvailabilitySource.UNKNOWN);

        CarPark near = park(1, "Near Big", 800, 174.767, -36.845);
        CarPark far  = park(2, "Far Small", 40, 174.780, -36.855);
        when(repo.findWithinRadius(anyDouble(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(List.of(
                        new CarParkDistance(near, 200),
                        new CarParkDistance(far, 1400)));

        var service = new RecommendationService(repo, occ);
        List<Recommendation> recs = service.recommend(174.7676, -36.8446, 1500, 8);

        assertThat(recs).hasSize(2);
        assertThat(recs.get(0).name()).isEqualTo("Near Big");
        assertThat(recs.get(0).score()).isGreaterThan(recs.get(1).score());
    }

    private static CarPark park(long id, String name, int total, double lon, double lat) {
        return new CarPark(id, name, null, "Covered Parking", null, null, null,
                total, null, null, null, null, lon, lat);
    }
}

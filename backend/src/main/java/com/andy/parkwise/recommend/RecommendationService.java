package com.andy.parkwise.recommend;

import com.andy.parkwise.domain.CarPark;
import com.andy.parkwise.domain.Recommendation;
import com.andy.parkwise.domain.Recommendation.AvailabilitySource;
import com.andy.parkwise.occupancy.OccupancyProvider;
import com.andy.parkwise.repo.CarParkRepository;
import com.andy.parkwise.repo.CarParkRepository.CarParkDistance;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Multi-factor ranking. Score blends walking distance, facility size, and a small
 * bonus when we actually know availability. All weights are explicit and explainable —
 * a recruiter can read exactly why park A ranked above park B.
 */
@Service
public class RecommendationService {

    private static final double WALK_METRES_PER_MIN = 80.0;
    private static final double W_DISTANCE = 0.60;
    private static final double W_SIZE     = 0.30;
    private static final double W_KNOWN    = 0.10;

    private final CarParkRepository repository;
    private final OccupancyProvider occupancyProvider;

    public RecommendationService(CarParkRepository repository, OccupancyProvider occupancyProvider) {
        this.repository = repository;
        this.occupancyProvider = occupancyProvider;
    }

    public List<Recommendation> recommend(double lon, double lat, double radiusM, int limit) {
        List<CarParkDistance> candidates = repository.findWithinRadius(lon, lat, radiusM, Math.max(limit, 20));
        return candidates.stream()
                .map(c -> toRecommendation(c, radiusM))
                .sorted(Comparator.comparingDouble(Recommendation::score).reversed())
                .limit(limit)
                .toList();
    }

    private Recommendation toRecommendation(CarParkDistance c, double radiusM) {
        CarPark p = c.carPark();
        double dist = c.distanceMeters();
        Optional<Integer> avail = occupancyProvider.availableSpaces(p);
        AvailabilitySource source = avail.isPresent() ? occupancyProvider.source() : AvailabilitySource.UNKNOWN;

        double distanceScore = 1 - Math.min(dist / radiusM, 1);
        double sizeScore = p.totalSpaces() == null ? 0 : Math.min(p.totalSpaces(), 900) / 900.0;
        double knownScore = avail.isPresent() ? 1 : 0;
        double score = W_DISTANCE * distanceScore + W_SIZE * sizeScore + W_KNOWN * knownScore;

        return new Recommendation(
                p.id(), p.name(), p.dataType(), p.totalSpaces(),
                avail.orElse(null), source,
                Math.round(dist), round1(dist / WALK_METRES_PER_MIN),
                round3(score), p.lon(), p.lat());
    }

    private static double round1(double v) { return Math.round(v * 10) / 10.0; }
    private static double round3(double v) { return Math.round(v * 1000) / 1000.0; }
}

package nz.parkwise.recommend;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import nz.parkwise.domain.Recommendation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GET /api/recommend?lat=-36.8446&lon=174.7676&radius=1500&limit=8
 * Ranked car parks near a destination.
 */
@RestController
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @GetMapping("/api/recommend")
    public List<Recommendation> recommend(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "1500") @Min(100) @Max(5000) double radius,
            @RequestParam(defaultValue = "8") @Min(1) @Max(50) int limit) {
        return service.recommend(lon, lat, radius, limit);
    }
}

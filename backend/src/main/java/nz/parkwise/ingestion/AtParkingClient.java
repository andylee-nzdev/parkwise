package nz.parkwise.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Thin client over the AT ParkingService FeatureServer.
 * Public endpoint, no auth. Returns raw GeoJSON as a Jackson tree so the
 * ingestion service can map fields explicitly.
 */
@Component
public class AtParkingClient {

    private final RestClient restClient;
    private final AtProperties props;

    public AtParkingClient(AtProperties props) {
        this.props = props;
        this.restClient = RestClient.builder().baseUrl(props.baseUrl()).build();
    }

    /** Fetch an entire layer as GeoJSON (WGS84). */
    public JsonNode fetchLayerGeoJson(int layer) {
        return restClient.get()
                .uri(uri -> uri.path("/{layer}/query")
                        .queryParam("where", "1=1")
                        .queryParam("outFields", "*")
                        .queryParam("outSR", "4326")
                        .queryParam("f", "geojson")
                        .build(layer))
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode fetchCarParks()   { return fetchLayerGeoJson(props.carParkLayer()); }
    public JsonNode fetchParkAndRide() { return fetchLayerGeoJson(props.parkAndRideLayer()); }
}

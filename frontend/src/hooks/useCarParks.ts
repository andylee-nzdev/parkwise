import { useEffect, useState } from "react";
import api from "../api/parkingApi.ts";
import type { CarPark } from "../types/CarPark.ts";

export interface Location {
    lat: number;
    lon: number;
}

// Auckland CBD — default search centre.
export const DEFAULT_CENTER: Location = { lat: -36.8446, lon: 174.7676 };

interface UseCarParksResult {
    carParks: CarPark[];
    center: Location;
    loading: boolean;
    error: string | null;
}

export function useCarParks(
    center: Location = DEFAULT_CENTER,
    radius = 1500,
    limit = 8,
): UseCarParksResult {
    const [carParks, setCarParks] = useState<CarPark[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let cancelled = false;
        // Reset request state before fetching. Fetching is a genuine external
        // sync, so the synchronous resets here are intentional.
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setLoading(true);
        setError(null);
        api.get<CarPark[]>(
            `api/recommend?lat=${center.lat}&lon=${center.lon}&radius=${radius}&limit=${limit}`,
        )
            .then((res) => {
                if (!cancelled) setCarParks(res.data);
            })
            .catch((err) => {
                if (!cancelled) setError(err.message ?? "Failed to load car parks");
            })
            .finally(() => {
                if (!cancelled) setLoading(false);
            });
        return () => {
            cancelled = true;
        };
    }, [center.lat, center.lon, radius, limit]);

    return { carParks, center, loading, error };
}

export interface CarPark {
    id: number;
    name: string;
    dataType: string;
    totalSpaces: number;
    availableSpaces: number | null;
    availabilitySource: string;
    distanceMeters: number;
    walkMinutes: number;
    score: number;
    lon: number;
    lat: number;
}

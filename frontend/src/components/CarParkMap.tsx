import { useEffect, useRef } from "react";
import * as maplibregl from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";
import type { CarPark } from "../types/CarPark.ts";
import type { Location } from "../hooks/useCarParks.ts";

// OpenStreetMap raster tiles — no API key required.
const MAP_STYLE: maplibregl.StyleSpecification = {
    version: 8,
    sources: {
        osm: {
            type: "raster",
            tiles: ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
            tileSize: 256,
            attribution: "© OpenStreetMap contributors",
        },
    },
    layers: [{ id: "osm", type: "raster", source: "osm" }],
};

interface CarParkMapProps {
    carParks: CarPark[];
    center: Location;
    selectedId?: number | null;
    onSelect?: (id: number) => void;
}

function availabilityColor(cp: CarPark): string {
    if (cp.availableSpaces == null) return "#6b7280"; // unknown — grey
    if (cp.availableSpaces === 0) return "#ef4444"; // full — red
    if (cp.availableSpaces < 10) return "#f59e0b"; // low — amber
    return "#22c55e"; // plenty — green
}

function popupHtml(cp: CarPark): string {
    const spaces =
        cp.availableSpaces == null
            ? "Availability unknown"
            : `${cp.availableSpaces} / ${cp.totalSpaces} spaces free`;
    const esc = (s: string) =>
        s.replace(/[&<>"]/g, (c) =>
            ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" })[c] as string,
        );
    return `
        <strong>${esc(cp.name)}</strong><br/>
        ${spaces}<br/>
        ${Math.round(cp.walkMinutes)} min walk (${Math.round(cp.distanceMeters)} m)
    `;
}

function CarParkMap({ carParks, center, selectedId, onSelect }: CarParkMapProps) {
    const containerRef = useRef<HTMLDivElement>(null);
    const mapRef = useRef<maplibregl.Map | null>(null);
    const markersRef = useRef<Map<number, maplibregl.Marker>>(new Map());
    const onSelectRef = useRef(onSelect);

    // Keep the latest onSelect callback without re-creating markers.
    useEffect(() => {
        onSelectRef.current = onSelect;
    }, [onSelect]);

    // Initialise the map once.
    useEffect(() => {
        if (!containerRef.current || mapRef.current) return;
        const markers = markersRef.current;
        const map = new maplibregl.Map({
            container: containerRef.current,
            style: MAP_STYLE,
            center: [center.lon, center.lat],
            zoom: 14,
        });
        map.addControl(new maplibregl.NavigationControl(), "top-right");
        mapRef.current = map;

        // The container may still be growing to fill its flex parent when the
        // map is created, leaving the canvas undersized. Keep it in sync.
        const resizeObserver = new ResizeObserver(() => map.resize());
        resizeObserver.observe(containerRef.current);

        return () => {
            resizeObserver.disconnect();
            map.remove();
            mapRef.current = null;
            markers.clear();
        };
        // Only run on mount; centre changes are handled below.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // Re-render markers whenever the car parks change.
    useEffect(() => {
        const map = mapRef.current;
        if (!map) return;

        markersRef.current.forEach((m) => m.remove());
        markersRef.current.clear();

        // Centre-of-search marker.
        const centerEl = document.createElement("div");
        centerEl.title = "Your location";
        Object.assign(centerEl.style, {
            width: "16px",
            height: "16px",
            borderRadius: "50%",
            background: "#3b82f6",
            border: "3px solid #fff",
            boxShadow: "0 0 0 2px rgba(59,130,246,0.5)",
        });
        new maplibregl.Marker({ element: centerEl })
            .setLngLat([center.lon, center.lat])
            .addTo(map);

        carParks.forEach((cp) => {
            const el = document.createElement("div");
            el.style.cursor = "pointer";
            el.innerHTML = `
                <svg width="30" height="40" viewBox="0 0 30 40" xmlns="http://www.w3.org/2000/svg">
                    <path d="M15 0C6.7 0 0 6.7 0 15c0 10 15 25 15 25s15-15 15-25C30 6.7 23.3 0 15 0z"
                        fill="${availabilityColor(cp)}" stroke="#fff" stroke-width="2"/>
                    <text x="15" y="20" text-anchor="middle" fill="#fff"
                        font-family="sans-serif" font-size="12" font-weight="bold">P</text>
                </svg>`;

            const marker = new maplibregl.Marker({ element: el, anchor: "bottom" })
                .setLngLat([cp.lon, cp.lat])
                .setPopup(
                    new maplibregl.Popup({ offset: 30 }).setHTML(popupHtml(cp)),
                )
                .addTo(map);

            el.addEventListener("click", () => onSelectRef.current?.(cp.id));
            markersRef.current.set(cp.id, marker);
        });

        // Fit the map to show all car parks plus the centre.
        const points = carParks.map(
            (cp) => [cp.lon, cp.lat] as [number, number],
        );
        points.push([center.lon, center.lat]);
        if (points.length > 1) {
            const bounds = points.reduce(
                (b, p) => b.extend(p),
                new maplibregl.LngLatBounds(points[0], points[0]),
            );
            map.fitBounds(bounds, { padding: 60, maxZoom: 16, duration: 0 });
        }
    }, [carParks, center.lat, center.lon]);

    // Open the popup for the externally-selected car park.
    useEffect(() => {
        if (selectedId == null) return;
        const marker = markersRef.current.get(selectedId);
        if (!marker) return;
        const map = mapRef.current;
        if (map) map.flyTo({ center: marker.getLngLat(), zoom: 16 });
        if (!marker.getPopup()?.isOpen()) marker.togglePopup();
    }, [selectedId]);

    return (
        <div
            ref={containerRef}
            style={{ width: "100%", height: "100%", minHeight: "400px" }}
        />
    );
}

export default CarParkMap;

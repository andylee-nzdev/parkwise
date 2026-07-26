import type { CarPark } from "../types/CarPark.ts";

interface CarParkListProps {
    carParks: CarPark[];
    selectedId?: number | null;
    onSelect?: (id: number) => void;
}

function availabilityLabel(cp: CarPark): { text: string; color: string } {
    if (cp.availableSpaces == null) return { text: "Unknown", color: "#6b7280" };
    if (cp.availableSpaces === 0) return { text: "Full", color: "#ef4444" };
    if (cp.availableSpaces < 10)
        return { text: `${cp.availableSpaces} free`, color: "#f59e0b" };
    return { text: `${cp.availableSpaces} free`, color: "#22c55e" };
}

function CarParkList({ carParks, selectedId, onSelect }: CarParkListProps) {
    if (carParks.length === 0) {
        return <p style={{ color: "var(--text)" }}>No car parks found nearby.</p>;
    }
    return (
        <ul style={{ listStyle: "none", padding: 0, margin: 0, width: "100%" }}>
            {carParks.map((cp) => {
                const availability = availabilityLabel(cp);
                const selected = cp.id === selectedId;
                return (
                    <li
                        key={cp.id}
                        onClick={() => onSelect?.(cp.id)}
                        style={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            justifyContent: "space-between",
                            gap: "12px",
                            padding: "12px 14px",
                            marginBottom: "8px",
                            borderRadius: "8px",
                            border: `1px solid ${selected ? "var(--accent)" : "var(--border)"}`,
                            background: selected ? "var(--accent-bg)" : "transparent",
                            cursor: "pointer",
                            textAlign: "left",
                        }}
                    >
                        <div style={{ alignItems: "flex-start", gap: "2px" }}>
                            <strong style={{ color: "var(--text-h)" }}>{cp.name}</strong>
                            <span style={{ fontSize: "14px" }}>
                                {Math.round(cp.walkMinutes)} min walk ·{" "}
                                {Math.round(cp.distanceMeters)} m
                            </span>
                        </div>
                        <span
                            style={{
                                flexShrink: 0,
                                fontSize: "14px",
                                fontWeight: 600,
                                color: availability.color,
                                whiteSpace: "nowrap",
                            }}
                        >
                            {availability.text}
                        </span>
                    </li>
                );
            })}
        </ul>
    );
}

export default CarParkList;

import { useState } from "react";
import CarParkList from "./components/CarParkList.tsx";
import CarParkMap from "./components/CarParkMap.tsx";
import { useCarParks } from "./hooks/useCarParks.ts";

function ParkWise() {
    const { carParks, center, loading, error } = useCarParks();
    const [selectedId, setSelectedId] = useState<number | null>(null);

    return (
        <div style={{ alignItems: "stretch", width: "100%", padding: "0 16px", boxSizing: "border-box" }}>
            <h1>ParkWise</h1>
            <div
                style={{
                    flexDirection: "row",
                    alignItems: "stretch",
                    gap: "16px",
                    width: "100%",
                    flexWrap: "wrap",
                }}
            >
                <div
                    style={{
                        flex: "1 1 320px",
                        minWidth: "280px",
                        alignItems: "stretch",
                        gap: "8px",
                    }}
                >
                    {loading && <p>Loading car parks…</p>}
                    {error && <p style={{ color: "#ef4444" }}>{error}</p>}
                    {!loading && !error && (
                        <CarParkList
                            carParks={carParks}
                            selectedId={selectedId}
                            onSelect={setSelectedId}
                        />
                    )}
                </div>
                <div
                    style={{
                        flex: "2 1 480px",
                        minWidth: "300px",
                        height: "600px",
                        maxHeight: "70vh",
                        borderRadius: "8px",
                        overflow: "hidden",
                        border: "1px solid var(--border)",
                    }}
                >
                    <CarParkMap
                        carParks={carParks}
                        center={center}
                        selectedId={selectedId}
                        onSelect={setSelectedId}
                    />
                </div>
            </div>
        </div>
    );
}

export default ParkWise;

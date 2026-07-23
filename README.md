# ParkWise — Smart Parking Recommender on Auckland Transport Open Data

A full-stack project that recommends the best car park near a destination in Auckland,
built on **Auckland Transport (AT) open data**. This repository is the **Spring Boot backend**.

> **The interesting engineering problem:** AT publishes **no public real-time occupancy API**.
> The available parking data is *static* — locations, sizes, fees, restrictions — with live
> availability for only **2 of 138** car parks. Rather than fake it, this project is designed
> *around* that gap: a PostGIS rules-and-distance recommender, an honest availability model
> behind a swappable interface, and (later) a self-built pipeline that estimates occupancy.

---

## Why this project exists

It's a portfolio piece demonstrating full-stack engineering for the New Zealand job market:
a **Java / Spring Boot** backend (10 years of Java depth) paired with a modern **React + TypeScript**
frontend (in a separate repo), solving a real local problem with real local data.

The point isn't CRUD — it's **engineering judgment under imperfect data**, which is what most
real backend work actually is.

---

## Architecture

```
                    React + TS frontend (separate repo)
                                │  GET /api/recommend
                                ▼
        ┌───────────────────────────────────────────────┐
        │                Spring Boot 3 (Java 21)          │
        │                                                 │
        │  RecommendationController                       │
        │        │                                        │
        │  RecommendationService  ── multi-factor scoring │
        │        │                                        │
        │  ┌─────┴───────┐        OccupancyProvider ◄─────┼── swappable strategy
        │  │ CarParkRepo │        ├ StaticOccupancyProvider (LIVE where AT feeds it)
        │  │ (JdbcClient)│        ├ SimulatedOccupancyProvider (ESTIMATED, demos)
        │  └─────┬───────┘        └ (Phase 3) model-backed  (ESTIMATED from history)
        │        │                                        │
        │  IngestionService ◄── AtParkingClient (RestClient)
        └────────┼───────────────────────────┬───────────┘
                 ▼                            ▼
        PostgreSQL + PostGIS         AT ParkingService FeatureServer
        (ST_DWithin radius search)   (public ArcGIS REST, no key, CC-BY-4.0)
```

### Key design decisions
- **`OccupancyProvider` interface** — the missing-API problem is isolated behind one seam.
  Static, simulated, and (future) ML providers are swapped via `occupancy.provider` config.
  If AT ever ships a real feed, it's a one-class change.
- **PostGIS for spatial search** — `ST_DWithin` over a GiST index does radius queries in the
  database; `ST_Distance` on `geography` gives correct metres. Explicit SQL, not hidden ORM magic.
- **Explainable ranking** — score = `0.6·distance + 0.3·size + 0.1·known-availability`, so any
  ranking can be justified. Honesty about availability is surfaced to the client as
  `LIVE` / `ESTIMATED` / `UNKNOWN`.
- **Explicit ingestion mapping** — each AT field is mapped by hand, so upstream schema drift
  is visible rather than silently swallowed.

---

## Tech stack
| Layer | Choice |
|---|---|
| Language / framework | Java 21, Spring Boot 3.3 |
| Data access | Spring `JdbcClient` (explicit SQL) |
| Database | PostgreSQL + PostGIS |
| Migrations | Flyway |
| External data | AT ParkingService (ArcGIS REST), via `RestClient` |
| Ops | Actuator health/metrics, Docker Compose |
| Tests | JUnit 5, Mockito, Testcontainers (integration) |

---

## Data source
```
Base : https://services2.arcgis.com/JkPEgZJGxhSjYOo0/arcgis/rest/services/ParkingService/FeatureServer
Layer 1  Car Parking     (138 points; ~26 real facilities)
Layer 5  Park & Ride     (25 sites; Bus/Train/Ferry — for the "park + transit" feature)
Layer 0  Parking Meter   (1006 on-street bays)
GeoJSON : {base}/{layer}/query?where=1=1&outFields=*&outSR=4326&f=geojson
Licence : CC-BY-4.0 — attribute "Auckland Transport"
```
No API key required. Verified live on 2026-07-22.

---

## Run it locally

**Option A — everything in Docker**
```bash
docker compose up --build
# app on :8080, PostGIS on :5432; DB is seeded from AT on first start
```

**Option B — DB in Docker, app from your IDE**
```bash
docker compose up db -d
cd backend && mvn spring-boot:run    # Flyway migrates, then seeds from AT if empty
```

Then:
```bash
curl "http://localhost:8080/api/recommend?lat=-36.8446&lon=174.7676&radius=1500&limit=8"
```
(see `backend/requests.http` for more examples)

### Demo the full UX where live data is absent
```bash
cd backend && mvn spring-boot:run -Doccupancy.provider=simulated
```
Availability now returns `ESTIMATED` values so the frontend can show the complete experience —
clearly labelled, never passed off as real.

---

## API
`GET /api/recommend`

| param | default | notes |
|---|---|---|
| `lat` | — | destination latitude (required) |
| `lon` | — | destination longitude (required) |
| `radius` | 1500 | metres, 100–5000 |
| `limit` | 8 | 1–50 |

Returns a ranked JSON array of car parks, each with `distanceMeters`, `walkMinutes`,
`availableSpaces`, `availabilitySource` (`LIVE`/`ESTIMATED`/`UNKNOWN`) and `score`.

---

## Project layout
```
docker-compose.yml       PostGIS + backend
backend/                 Spring Boot API (Java 21, Maven)
├── Dockerfile
├── pom.xml
├── requests.http
└── src/main/java/nz/parkwise
    ├── ParkWiseApplication.java
    ├── domain/          CarPark, Recommendation
    ├── ingestion/       AtParkingClient, IngestionService, StartupIngestRunner, AtProperties
    ├── occupancy/       OccupancyProvider (+ Static / Simulated implementations)
    ├── recommend/       RecommendationController, RecommendationService
    └── repo/            CarParkRepository (PostGIS ST_DWithin)
backend/src/main/resources
├── application.yml
└── db/migration/V1__init.sql
```
`frontend/` will sit alongside `backend/` once the client app lands.

---

## Roadmap
- **Phase 1 (this repo)** — ingestion, PostGIS radius recommender, `/api/recommend`. ✅ MVP
- **Phase 2** — natural-language query endpoint (LLM structured output → params) + rules RAG.
- **Phase 3** — occupancy time-series pipeline (`occupancy_reading`) + a model-backed
  `OccupancyProvider` returning probabilistic `ESTIMATED` availability.
- **Phase 4** — Park & Ride "drive + public transport" trips using AT GTFS / Realtime APIs.

---

## Status
Week 0–1 skeleton. Data dependency verified against the live AT endpoint before any code was
written — the spatial query and ranking are proven on real data (see `../parking-poc`).

*Data © Auckland Transport, CC-BY-4.0. This is an independent student project, not affiliated with AT.*

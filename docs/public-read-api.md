# Public read API (current paths)

These existing paths are under the Spring context path `/api`. Anonymous GET requests are allowed. Other methods remain protected. The frontend calls the backend directly for server rendered pages using `BACKEND_URL` (default `http://localhost:8080`) and `cache: "no-store"`.

The canonical trip reads are:

| Request | Response |
| --- | --- |
| `GET /api/trips?page=0&size=5` | `{"trips":[...],"numPages":1}`; `page` is zero based and `size` is 1 through 5. Default is page 0, size 5. |
| `GET /api/trips/1` | One trip detail object with the fields listed below. |
| `GET /api/trips/routes` | A GeoJSON FeatureCollection with a `features` array. |

Trips are ordered by trip date descending, then ID descending, so pagination is stable when dates match. Invalid parameters return `400`, missing trips return `404`, and errors use `{"message":"..."}`. See [OpenAPI](openapi.yaml) for schemas and examples. The old GET paths below remain as aliases while the frontend migrates.

| Request | Successful response shape |
| --- | --- |
| `GET /api/getTrips?pageSize=3&pageNum=0` | `{"trips":[{"id":1,"title":"...","desc":"...","locationName":"...","stateCountry":"...","dateOfTrip":"2025-01-01","imageURL":"..."}],"numPages":1}` |
| `GET /api/getTrip?id=1` | `{"id":1,"title":"...","desc":"...","locationName":"...","stateCountry":"...","dateOfTrip":"2025-01-01","datePosted":"2025-01-02","dateUpdated":"2025-01-02","authorUsername":"nick","peopleInvolved":["nick","friend"],"route":null,"location":[-111.4,45.3],"imageURL":"...","authorName":"Nick Clouse","companionNames":["Friend Name"]}`. `route` is a GeoJSON LineString when present. The name list excludes the author. |
| `GET /api/getAllTripFeatureRoutes` | `{"type":"FeatureCollection","features":[{"type":"Feature","properties":{"id":1,"title":"...","date":"2025-01-01","locationName":"...","distance":1.2,"elevation":500},"geometry":{"type":"LineString","coordinates":[[-111.4,45.3]]}}]}` |
| `GET /api/getDaysSkied?year=2024` | `[ {"firstName":"...","daysSkied":2,"year":2024} ]` |
| `GET /api/getDaysSkiedLocation?year=2024` | `[ {"firstName":"...","daysSkied":2,"location":"...","year":2024} ]` |

For no trips, `getTrips` returns `200` with `{"trips":[],"numPages":0}`. For no routes, `getAllTripFeatureRoutes` returns `200` with an empty `features` array. Stats endpoints return an empty array when no data matches. `pageNum` is zero based and must be nonnegative. `pageSize` must be between 1 and 5. Invalid parameters return `400` with `{"message":"..."}`; a missing trip returns `404` with the same error shape. Unauthenticated writes return `401` with `{"message":"Authentication required."}`. The API keeps these paths until callers migrate in a later roadmap step.

## Local database setup

Create an empty PostgreSQL database with PostGIS available to the database user. Flyway creates the PostGIS extension and application tables on first start; Hibernate validates the result on every start. The database user needs permission to create the extension, or a database administrator must install PostGIS first. Set the local datasource and JWT values in the Git-ignored `ig_api/src/main/resources/application.properties`, with `spring.jpa.hibernate.ddl-auto=validate`. Start with the `dev` profile to add demo data only when all application tables are empty. Restarting with `dev` keeps those rows; without `dev`, no demo data is added. Never use `create-drop` after storing data you want to keep.

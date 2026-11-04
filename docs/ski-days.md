# Ski day logging

Signed-in users open `/post/day` to log a required calendar date at an existing location or add a location with a required name and country. State is required for US locations and omitted for other countries. Supported countries are the US, Canada, Japan, France, Italy, Switzerland, and Norway. Coordinates are optional and are not collected here. The Post menu's Trip choice is reserved for `/post/trip` in roadmap item 4.

The API paths below are under `/api`. Writes use the existing JWT cookie, JSON requests, and the configured allowed browser origin. Personal entries are private; location choices and aggregate statistics are public.

| Request | Example response |
| --- | --- |
| `GET /api/locations` | `[{"id":1,"name":"Beehive Basin","state":"MT","country":"US"}]` |
| `GET /api/locations/options` | `{"states":[{"value":"MT","label":"Montana"}],"countries":[{"value":"US","label":"United States"}]}` (other options omitted) |
| `POST /api/locations` with `{"name":"Beehive Basin","state":"MT","country":"US"}` | `201` with the location object |
| `GET /api/me/ski-days` | `{"seasonStartYear":2025,"totalDays":1,"entries":[{"id":4,"date":"2025-11-10","locationId":1,"locationName":"Beehive Basin"}]}` |
| `POST /api/me/ski-days` with `{"date":"2025-01-10","locationId":1}` | `201` with the entry object |
| `PATCH /api/me/ski-days/4` with the same date and location fields | `200` with the updated entry |
| `DELETE /api/me/ski-days/4` | `204` with no body |

The season runs October 1 through September 30. Overall totals count distinct `(user, date)` across trips and manual entries. Location totals count distinct `(user, date, location)`, so location bars can sum to more than the overall total. Deleting a manual entry does not remove a day still represented by a trip. Duplicate entries for the same user, date, and location return `409`; another user's entry cannot be changed (`403`).

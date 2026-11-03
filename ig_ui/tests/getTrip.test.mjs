import assert from "node:assert/strict";
import { test } from "node:test";
import { getTrip, tripRouteCollection } from "../src/lib/getTrip.mjs";
import { PublicNotFoundError } from "../src/lib/publicApi.mjs";

test("trip detail read uses its id and preserves an absent route", async () => {
  const previous = process.env.BACKEND_URL;
  process.env.BACKEND_URL = "http://api.example:8080";
  try {
    let requestedUrl;
    const trip = await getTrip(7, async (url) => {
      requestedUrl = url;
      return new Response(JSON.stringify({ id: 7, title: "A trip", route: null }), {
        headers: { "content-type": "application/json" },
      });
    });
    assert.equal(requestedUrl, "http://api.example:8080/api/getTrip?id=7");
    assert.equal(trip.route, null);
  } finally {
    if (previous === undefined) delete process.env.BACKEND_URL;
    else process.env.BACKEND_URL = previous;
  }
});

test("a missing trip is distinguishable from a backend failure", async () => {
  await assert.rejects(
    getTrip(999, async () => new Response("missing", { status: 404 })),
    PublicNotFoundError,
  );
  await assert.rejects(
    getTrip(999, async () => new Response("failure", { status: 503 })),
    /unavailable/i,
  );
});

test("the detail map appears only for a usable route", () => {
  const trip = { id: 7, title: "A trip", dateOfTrip: "2025-01-01", locationName: "Big Sky" };
  assert.equal(tripRouteCollection({ ...trip, route: null }), null);
  assert.equal(tripRouteCollection({ ...trip, route: { type: "LineString", coordinates: [] } }), null);
  const collection = tripRouteCollection({
    ...trip,
    route: { type: "LineString", coordinates: [[-111.4, 45.3], [-111.5, 45.4]] },
  });
  assert.equal(collection.features[0].properties.id, 7);
  assert.deepEqual(collection.features[0].geometry.coordinates[0], [-111.4, 45.3]);
});

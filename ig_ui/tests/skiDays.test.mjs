import assert from "node:assert/strict";
import {test} from "node:test";
import {saveSkiDay, deleteSkiDay} from "../src/lib/skiDays.mjs";

const json = (body, status = 200) => new Response(JSON.stringify(body), {
  status, headers: {"content-type": "application/json"},
});

test("a new location is created before logging the day", async () => {
  const requests = [];
  const fetcher = async (url, options) => {
    requests.push({url, ...options});
    return url === "/api/locations" ? json({id: 8}, 201) : json({id: 4}, 201);
  };
  await saveSkiDay({form: {date: "2025-01-10", name: "Beehive", state: "MT", country: "US"}, newLocation: true}, fetcher);
  assert.deepEqual(requests.map(({url, method}) => [url, method]), [["/api/locations", "POST"], ["/api/me/ski-days", "POST"]]);
  assert.deepEqual(JSON.parse(requests[1].body), {date: "2025-01-10", locationId: 8});
});

test("editing an existing day updates it without creating a location", async () => {
  let request;
  await saveSkiDay({form: {date: "2025-01-11", locationId: "2"}, editingId: 4, newLocation: false},
    async (url, options) => { request = {url, ...options}; return json({id: 4}); });
  assert.equal(request.url, "/api/me/ski-days/4");
  assert.equal(request.method, "PATCH");
  assert.deepEqual(JSON.parse(request.body), {date: "2025-01-11", locationId: 2});
});

test("a non-US location does not send a stale US state", async () => {
  let locationRequest;
  await saveSkiDay({form: {date: "2025-01-10", name: "Chamonix", state: "MT", country: "FR"}, newLocation: true},
    async (url, options) => {
      if (url === "/api/locations") locationRequest = JSON.parse(options.body);
      return json({id: 8}, 201);
    });
  assert.deepEqual(locationRequest, {name: "Chamonix", state: null, country: "FR"});
});

test("deleting a day accepts an empty success response and reports API failures", async () => {
  await deleteSkiDay(4, async () => new Response(null, {status: 204}));
  await assert.rejects(deleteSkiDay(4, async () => json({message: "Forbidden"}, 403)), /Forbidden/);
});

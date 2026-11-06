import assert from "node:assert/strict";
import {test} from "node:test";
import {createTrip} from "../src/lib/tripCreate.mjs";

test("trip creation sends Lexical JSON and returns the new id", async () => {
  let request;
  const body = {root: {type: "root", children: []}};
  const result = await createTrip({title: "A trip", dateOfTrip: "2025-01-10", locationId: "2", participantIds: [3], body},
    async (url, options) => {
      request = {url, ...options};
      return new Response(JSON.stringify({id: 7}), {status: 201, headers: {"content-type": "application/json"}});
    });
  assert.equal(result.id, 7);
  assert.equal(request.url, "/api/trips");
  assert.equal(request.method, "POST");
  assert.equal(request.credentials, "include");
  assert.deepEqual(JSON.parse(request.body), {title: "A trip", dateOfTrip: "2025-01-10", locationId: 2,
    newLocation: null, participantIds: [3], body, route: null});
});

test("trip creation preserves validation messages from the API", async () => {
  await assert.rejects(createTrip({title: "", dateOfTrip: "2025-01-10", locationId: 2,
    body: {root: {type: "root", children: []}}},
  async () => new Response(JSON.stringify({message: "Enter a trip title."}), {
    status: 400, headers: {"content-type": "application/json"},
  })), /Enter a trip title/);
});

test("a new location is sent with the trip in one request", async () => {
  const requests = [];
  const body = {root: {type: "root", children: []}};
  await createTrip({title: "A trip", dateOfTrip: "2025-01-10", body,
    newLocation: {name: "Chamonix", country: "FR", state: "MT"}},
  async (url, options) => {
    requests.push({url, ...options});
    return new Response(JSON.stringify({id: 7}), {status: 201, headers: {"content-type": "application/json"}});
  });
  assert.deepEqual(requests.map(({url, method}) => [url, method]), [["/api/trips", "POST"]]);
  assert.deepEqual(JSON.parse(requests[0].body), {title: "A trip", dateOfTrip: "2025-01-10",
    locationId: null, newLocation: {name: "Chamonix", country: "FR", state: null},
    participantIds: [], body, route: null});
});

test("a failed trip request cannot leave a location created by a separate browser request", async () => {
  const requests = [];
  await assert.rejects(createTrip({title: "A trip", dateOfTrip: "2025-01-10", body: {},
    newLocation: {name: "Beehive", country: "US", state: "MT"}},
  async (url, options) => {
    requests.push({url, ...options});
    return new Response(JSON.stringify({message: "That location already exists."}), {status: 409, headers: {"content-type": "application/json"}});
  }), /That location already exists/);
  assert.deepEqual(requests.map(({url}) => url), ["/api/trips"]);
  assert.deepEqual(JSON.parse(requests[0].body).newLocation, {name: "Beehive", country: "US", state: "MT"});
});

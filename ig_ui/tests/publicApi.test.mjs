import assert from "node:assert/strict";
import { test } from "node:test";
import { getPublicJson } from "../src/lib/publicApi.mjs";

test("public server reads use the configured backend and no-store", async () => {
  const previous = process.env.BACKEND_URL;
  process.env.BACKEND_URL = "http://api.example:8080";
  try {
    let requestedUrl;
    let requestedOptions;
    const data = await getPublicJson("getTrips?pageSize=3&pageNum=0", async (url, options) => {
      requestedUrl = url;
      requestedOptions = options;
      return new Response(JSON.stringify({ trips: [], numPages: 0 }), {
        headers: { "content-type": "application/json" },
      });
    });
    assert.equal(requestedUrl, "http://api.example:8080/api/getTrips?pageSize=3&pageNum=0");
    assert.equal(requestedOptions.cache, "no-store");
    assert.deepEqual(data, { trips: [], numPages: 0 });
  } finally {
    if (previous === undefined) delete process.env.BACKEND_URL;
    else process.env.BACKEND_URL = previous;
  }
});

test("failed backend reads reject instead of pretending the result is empty", async () => {
  await assert.rejects(
    getPublicJson("getTrips", async () => new Response("failure", { status: 503 })),
    /unavailable|failed/i,
  );
});

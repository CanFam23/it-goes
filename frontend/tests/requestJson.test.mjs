import assert from "node:assert/strict";
import { test } from "node:test";
import { requestJson, SERVER_UNAVAILABLE } from "../src/lib/requestJson.mjs";

test("shows the API message for a JSON error", async () => {
  const fetcher = async () => new Response(JSON.stringify({ message: "Username is already taken." }), {
    status: 409,
    headers: { "content-type": "application/json" },
  });

  await assert.rejects(requestJson("/api/auth/signup", {}, fetcher), {
    message: "Username is already taken.",
  });
});

test("hides non-JSON proxy errors", async () => {
  const fetcher = async () => new Response("<html>Bad Gateway</html>", { status: 502 });

  await assert.rejects(requestJson("/api/auth/signup", {}, fetcher), {
    message: SERVER_UNAVAILABLE,
  });
});

test("hides network errors", async () => {
  const fetcher = async () => { throw new TypeError("Failed to fetch"); };

  await assert.rejects(requestJson("/api/access-codes", {}, fetcher), {
    message: SERVER_UNAVAILABLE,
  });
});

test("hides malformed JSON", async () => {
  const fetcher = async () => new Response("not json", {
    status: 400,
    headers: { "content-type": "application/json" },
  });

  await assert.rejects(requestJson("/api/auth/signup", {}, fetcher), {
    message: "Something went wrong. Please try again.",
  });
});

test("explains when the invite session is no longer valid", async () => {
  const fetcher = async () => new Response("", { status: 401 });

  await assert.rejects(requestJson("/api/access-codes", {}, fetcher), {
    message: "Please log in and try again.",
  });
});

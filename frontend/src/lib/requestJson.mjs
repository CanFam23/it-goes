export const SERVER_UNAVAILABLE = "The server is unavailable. Please try again shortly.";
const REQUEST_FAILED = "Something went wrong. Please try again.";
const SIGN_IN_REQUIRED = "Please log in and try again.";

export async function requestJson(url, options, fetcher = fetch) {
  let response;
  try {
    response = await fetcher(url, options);
  } catch {
    throw new Error(SERVER_UNAVAILABLE);
  }

  if (!response.ok && response.status >= 500) {
    throw new Error(SERVER_UNAVAILABLE);
  }
  if (!response.ok && (response.status === 401 || response.status === 403)) {
    throw new Error(SIGN_IN_REQUIRED);
  }
  if (!response.headers.get("content-type")?.includes("application/json")) {
    throw new Error(REQUEST_FAILED);
  }

  let data;
  try {
    data = await response.json();
  } catch {
    throw new Error(REQUEST_FAILED);
  }

  if (!response.ok) {
    throw new Error(typeof data?.message === "string" && data.message.trim()
      ? data.message
      : REQUEST_FAILED);
  }

  return data;
}

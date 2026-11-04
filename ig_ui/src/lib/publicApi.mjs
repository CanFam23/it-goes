export class PublicNotFoundError extends Error {}

export async function getPublicJson(path, fetcher = fetch) {
  const backendUrl = process.env.BACKEND_URL || "http://localhost:8080";
  let response;
  try {
    response = await fetcher(`${backendUrl}/api/${path}`, { cache: "no-store" });
  } catch {
    throw new Error("The server is unavailable. Please try again shortly.");
  }
  if (response.status === 404) {
    throw new PublicNotFoundError("The requested page was not found.");
  }
  if (!response.ok) {
    throw new Error("The server is unavailable. Please try again shortly.");
  }
  try {
    return await response.json();
  } catch {
    throw new Error("The server returned an invalid response.");
  }
}

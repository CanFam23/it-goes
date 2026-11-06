import {requestJson} from "./requestJson.mjs";

export async function createTrip({title, dateOfTrip, locationId, newLocation = null, participantIds = [], body, route = null},
  fetcher = fetch) {
  return requestJson("/api/trips", {
    method: "POST", credentials: "include", headers: {"Content-Type": "application/json"},
    body: JSON.stringify({title, dateOfTrip, locationId: newLocation ? null : Number(locationId),
      newLocation: newLocation ? {name: newLocation.name, country: newLocation.country,
        state: newLocation.country === "US" ? newLocation.state : null} : null,
      participantIds, body, route}),
  }, fetcher);
}

export function parseGpx(xml) {
  const doc = new DOMParser().parseFromString(xml, "application/xml");
  if (doc.getElementsByTagName("parsererror").length || doc.documentElement?.localName !== "gpx") {
    throw new Error("Choose a valid GPX file.");
  }
  let points = [...doc.getElementsByTagNameNS("*", "trkpt")];
  if (!points.length) points = [...doc.getElementsByTagNameNS("*", "rtept")];
  if (points.length < 2 || points.length > 5000) throw new Error("The GPX route needs 2–5000 points.");
  const coordinates = points.map((point) => {
    const lon = Number(point.getAttribute("lon"));
    const lat = Number(point.getAttribute("lat"));
    if (!Number.isFinite(lon) || !Number.isFinite(lat) || Math.abs(lon) > 180 || Math.abs(lat) > 90
        || point.getAttribute("lon") === null || point.getAttribute("lat") === null) {
      throw new Error("The GPX route has invalid coordinates.");
    }
    return [lon, lat];
  });
  return {type: "LineString", coordinates};
}

import { getPublicJson } from "./publicApi.mjs";

export function getTrip(id, fetcher = fetch) {
  return getPublicJson(`getTrip?id=${encodeURIComponent(id)}`, fetcher);
}

export function tripRouteCollection(trip) {
  const route = trip.route;
  if (route?.type !== "LineString" || !Array.isArray(route.coordinates)
      || route.coordinates.length < 2
      || route.coordinates.some((point) => !Array.isArray(point)
        || point.length < 2 || !point.slice(0, 2).every(Number.isFinite)
        || Math.abs(point[0]) > 180 || Math.abs(point[1]) > 90)) {
    return null;
  }
  return {
    type: "FeatureCollection",
    features: [{
      type: "Feature",
      properties: { id: trip.id, title: trip.title, date: trip.dateOfTrip,
        locationName: trip.locationName },
      geometry: route,
    }],
  };
}

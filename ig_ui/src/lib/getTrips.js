import { getPublicJson } from "./publicApi.mjs";

export async function getTrips({
                                       pageSize = 3,
                                       pageNum = 0,
                                     } = {}) {
  const data = await getPublicJson(`getTrips?pageSize=${encodeURIComponent(pageSize)}&pageNum=${encodeURIComponent(pageNum)}`);
  return { trips: data.trips, totalPages: data.numPages };
}

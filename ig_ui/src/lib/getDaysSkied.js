import { getPublicJson } from "./publicApi.mjs";

export async function getDaysSkied(route,year){
  if (route !== "getDaysSkied" && route !== "getDaysSkiedLocation") {
    throw new Error("Invalid statistics route.");
  }
  return getPublicJson(`${route}?year=${encodeURIComponent(year)}`);
}

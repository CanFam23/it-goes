import { getPublicJson } from "./publicApi.mjs";

export async function getTripRoutes(){
  return getPublicJson("getAllTripFeatureRoutes");
}

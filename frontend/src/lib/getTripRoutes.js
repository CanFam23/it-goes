import {headers} from "next/headers";

export async function getTripRoutes(){
  // TODO Error handling
  try {
    // Build the base URL from request headers
    const h = await headers();
    const proto = h.get('x-forwarded-proto') || 'http';
    const host = h.get('host');
    const base = `${proto}://${host}`;

    // API route
    const res = await fetch(
      `${base}/api/getAllTripFeatureRoutes`,
    );

    if (!res.ok) {
      console.warn('Error fetching data', res.status, res.statusText);
      return {};
    }

    const data = await res.json();
    console.log(`Successfully retrieved data`);
    return data;
  } catch (err) {
    console.warn(`Failed to fetch data:`, err);
    return {};
  }
}
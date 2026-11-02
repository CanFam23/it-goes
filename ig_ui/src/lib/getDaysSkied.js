import {headers} from "next/headers";

export async function getDaysSkied(route,year){
  /**
   * Given a year, calls the getDaysSkied api route to get the number of days each
   * user in the database has skied.
   */
  // TODO Error handling
  try {
    // Build the base URL from request headers
    const h = await headers();
    const proto = h.get('x-forwarded-proto') || 'http';
    const host = h.get('host');
    const base = `${proto}://${host}`;

    // API route
    const res = await fetch(
      `${base}/api/${route}?year=${year}`,
      { cache: 'force-cache' }
    );

    if (!res.ok) {
      console.warn('Error fetching data from ',route, res.status, res.statusText);
      return { trips: [], total: 0 };
    }

    const data = await res.json();
    console.log(`Successfully retrieved data from ${route}`);
    return data;
  } catch (err) {
    console.warn(`Failed to fetch data from ${route}:`, err);
    return {};
  }
}
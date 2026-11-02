import { headers } from 'next/headers';

export async function getTrips({
                                       pageSize = 3,
                                       pageNum = 0,
                                     } = {}) {
  /**
   * Given a page size and number, calls the getTrips api route to get the
   * requested trips.
   */
  try {
    // Build the base URL from request headers
    const h = await headers();
    const proto = h.get('x-forwarded-proto') || 'http';
    const host = h.get('host');
    const base = `${proto}://${host}`; // e.g. http://localhost:3000

    // API route
    const res = await fetch(
      `${base}/api/getTrips?pageSize=${pageSize}&pageNum=${pageNum}`,
      { cache: 'force-cache' }
    );

    if (!res.ok) {
      console.warn('Error fetching trips:', res.status, res.statusText);
      return { trips: [], total: 0 };
    }

    const data = await res.json();
    console.log(`Successfully retrieved ${data.trips.length} trips`);
    return {
      trips: data.trips,
      totalPages: data.numPages,
    };
  } catch (err) {
    console.warn('Failed to fetch recent trips:', err);
    return { trips: [], total: 0 };
  }
}

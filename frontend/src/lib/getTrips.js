import { headers } from 'next/headers';

export async function getTrips({
                                       pageSize = 3,
                                       pageNum = 0,
                                     } = {}) {
  try {
    // Build the base URL from request headers
    const h = await headers();
    const proto = h.get('x-forwarded-proto') || 'http';
    const host = h.get('host');
    const base = `${proto}://${host}`; // e.g. http://localhost:3000

    // API route
    const res = await fetch(
      `${base}/api/getRecentTrips?pageSize=${pageSize}&pageNum=${pageNum}`,
      { cache: 'no-store' } // or { next: { revalidate: 60 } }
    );

    if (!res.ok) {
      console.error('Error fetching trips:', res.status, res.statusText);
      return { trips: [], total: 0 };
    }

    const data = await res.json();
    console.log(`Successfully retrieved ${data.trips.length} trips`);
    return {
      trips: data.trips,
      totalPages: data.numPages,
    };
  } catch (err) {
    console.error('Failed to fetch recent trips:', err);
    return { trips: [], total: 0 };
  }
}

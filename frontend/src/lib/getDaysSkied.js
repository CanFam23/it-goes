import {headers} from "next/headers";

export async function getDaysSkied(year){
  // TODO Error handling
  try {
    // Build the base URL from request headers
    const h = await headers();
    const proto = h.get('x-forwarded-proto') || 'http';
    const host = h.get('host');
    const base = `${proto}://${host}`;

    // API route
    const res = await fetch(
      `${base}/api/getDaysSkied?year=${year}`,
      { cache: 'no-store' }
    );

    if (!res.ok) {
      console.error('Error fetching days skied:', res.status, res.statusText);
      return { trips: [], total: 0 };
    }

    const data = await res.json();
    console.log(`Successfully retrieved days skied data`);
    return data;
  } catch (err) {
    console.error('Failed to fetch days skied:', err);
    return {};
  }
}
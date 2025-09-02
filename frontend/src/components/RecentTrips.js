import {headers} from "next/headers";
import TripSummaryCard from "@/components/TripSummaryCard";

export default async function RecentTrips({tripsParam, page}){
  const h = await headers();
  const proto = h.get('x-forwarded-proto') || 'http';
  const host = h.get('host');
  const base = `${proto}://${host}`; // e.g. http://localhost:3000 in dev

  // Default values if parameter isn't given
  const numTrips = tripsParam ? tripsParam: 3;
  const pageNum = page ? page: 1;

  const res = await fetch(`${base}/api/getRecentTrips?numTrips=${numTrips}&page=${pageNum}`, {
    cache: 'no-store', // or: next: { revalidate: 60 }
  });
  if (!res.ok) {
    console.log(res);
    return (
      <p className="error-msg">
        Error fetching recent trips
      </p>
    )
  }

  const tripData = await res.json();

  const trips = tripData.trips;

  console.log(`Successfully retrieved trips`);
  console.log(trips);

  return (
    <div>
      {Object.entries(trips).map(([key, trip]) => (
        <TripSummaryCard key={key} trip={trip}/>
      ))}
    </div>
  );

}
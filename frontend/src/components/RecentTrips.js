import {headers} from "next/headers";
import TripSummaryCard from "@/components/TripSummaryCard";

export default async function RecentTrips({trips}){
  return (
    <div>
      {Object.entries(trips).map(([key, trip]) => (
        <TripSummaryCard key={key} trip={trip}/>
      ))}
    </div>
  );

}
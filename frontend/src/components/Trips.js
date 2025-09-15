import {headers} from "next/headers";
import TripSummaryCard from "@/components/TripSummaryCard";

export default function Trips({trips}){
  if (!trips || trips.length === 0){
    return <p className="error-msg">No recent trips found</p>
  }

  return (
    <div className="w-full">
      {Object.entries(trips).map(([key, trip]) => (
        <TripSummaryCard key={key} trip={trip}/>
      ))}
    </div>
  );

}
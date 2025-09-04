import {headers} from "next/headers";
import TripSummaryCard from "@/components/TripSummaryCard";

export default async function Trips({trips}){
  return (
    <div className="w-full">
      {Object.entries(trips).map(([key, trip]) => (
        <TripSummaryCard key={key} trip={trip}/>
      ))}
    </div>
  );

}
import Map from "@/components/Map";
import {getTripRoutes} from "@/lib/getTripRoutes";

import {metadata} from "@/app/layout";

export function generateMetadata({ searchParams }) {
  return {
    title: "Map",
    description: metadata.description,
  };
}

export default async function page(){
  const tripRoutes = await getTripRoutes();

  return (
    <Map routeData={tripRoutes}/>
  );
}
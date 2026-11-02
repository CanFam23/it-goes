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

  if (!tripRoutes.features?.length) {
    return <p className="error-msg">No trip routes to show yet.</p>;
  }

  return (
    <Map routeData={tripRoutes}/>
  );
}

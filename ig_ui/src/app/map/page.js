import Map from "@/components/Map";
import {getTripRoutes} from "@/lib/getTripRoutes";
import ErrorMessage from "@/components/ErrorMessage";

import {metadata} from "@/app/layout";

export function generateMetadata({ searchParams }) {
  return {
    title: "Map",
    description: metadata.description,
  };
}

export default async function page(){
  let tripRoutes;
  try {
    tripRoutes = await getTripRoutes();
  } catch {
    return <ErrorMessage message="Unable to load trip routes. Please try again shortly." />;
  }

  if (!tripRoutes.features?.length) {
    return <p className="error-msg">No trip routes to show yet.</p>;
  }

  return (
    <Map routeData={tripRoutes}/>
  );
}

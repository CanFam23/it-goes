import Map from "@/components/Map";
import {getTripRoutes} from "@/lib/getTripRoutes";

export default async function page(){
  const tripRoutes = await getTripRoutes();

  return (
    <Map routeData={tripRoutes}/>
  );
}
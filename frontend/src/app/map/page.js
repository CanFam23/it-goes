import Map from "@/components/Map";
import {getTripRoutes} from "@/lib/getTripRoutes";

export default async function page(){
  const tripRoutes = await getTripRoutes();
  console.log(tripRoutes.route);
  return (
    <Map location={tripRoutes.location} route={tripRoutes.route}/>
  );
}
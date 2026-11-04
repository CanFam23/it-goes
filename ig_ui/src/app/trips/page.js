import Trips from "@/components/Trips";
import Pagination from "@/components/Pagination";
import {getTrips} from "@/lib/getTrips";
import Title from "@/components/Title";
import DaysSkiedChart from "@/components/DaysSkiedChart";
import {getDaysSkied} from "@/lib/getDaysSkied";
import DaysSkiedTotal from "@/components/DaysSkiedTotal";
import ErrorMessage from "@/components/ErrorMessage";

import {metadata} from "@/app/layout";

export function generateMetadata({ searchParams }) {
  return {
    title: "Recent Trips",
    description: metadata.description,
  };
}

export default async function Page({ searchParams }) {
  const searchParam = await searchParams;
  const pageSize = searchParam.pageSize ? (searchParam.pageSize): 5;
  const page = searchParam.page ? (searchParam.page) : 0;

  const year = 2024;
  let trips, totalPages, daysSkiedLocation, daysSkied;
  try {
    [{trips, totalPages}, daysSkiedLocation, daysSkied] = await Promise.all([
      getTrips({pageSize, pageNum: page}),
      getDaysSkied("getDaysSkiedLocation", year),
      getDaysSkied("getDaysSkied", year),
    ]);
  } catch {
    return <ErrorMessage message="Unable to load trips or stats. Please try again shortly." />;
  }

  return (
    <section className="w-full">
      <Title title="Trips"/>

      <div className="bg-background p-4 shadow-lg">
        <h3 className="font-black text-md md:text-xl lg:text-2xl place-self-center">Stats for the {year}-{year+1} Season</h3>
        <hr className="border-t border-black my-4"/>
        <DaysSkiedTotal daysSkiedData={daysSkied} year={year}/>
        <DaysSkiedChart plotData={daysSkiedLocation} year={year}/>
        <p className="text-center text-sm">A date counts once in the total; skiing two locations that day counts once at each location.</p>
      </div>

      <Trips trips={trips}/>

      <Pagination
        page={page}
        pageSize={pageSize}
        totalPages={totalPages ? totalPages: 0}
        basePath="/trips"
      />
    </section>
  );
}

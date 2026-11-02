import Trips from "@/components/Trips";
import Pagination from "@/components/Pagination";
import {getTrips} from "@/lib/getTrips";
import Title from "@/components/Title";
import DaysSkiedChart from "@/components/DaysSkiedChart";
import {getDaysSkied} from "@/lib/getDaysSkied";
import DaysSkiedTotal from "@/components/DaysSkiedTotal";

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

  const {trips, totalPages} = await getTrips({pageSize: pageSize, pageNum: page});

  const year = 2024;

  const daysSkiedLocation = await getDaysSkied("getDaysSkiedLocation",year);

  const daysSkied = await getDaysSkied("getDaysSkied",year);

  return (
    <section className="w-full">
      <Title title="Trips"/>

      <div className="bg-background p-4 shadow-lg">
        <h3 className="font-black text-md md:text-xl lg:text-2xl place-self-center">Stats for the {year}-{year+1} Season</h3>
        <hr className="border-t border-black my-4"/>
        <DaysSkiedTotal daysSkiedData={daysSkied} year={year}/>
        <DaysSkiedChart plotData={daysSkiedLocation} year={year}/>
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

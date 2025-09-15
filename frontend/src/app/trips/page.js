import Trips from "@/components/Trips";
import Pagination from "@/components/Pagination";
import ErrorMessage from "@/components/ErrorMessage";
import {getTrips} from "@/lib/getTrips";
import Title from "@/components/Title";
import DaysSkiedChart from "@/components/DaysSkiedChart";
import {getDaysSkied} from "@/lib/getDaysSkied";

export default async function Page({ searchParams }) {
  const searchParam = await searchParams;
  console.log("Trip Page: searchParams:", JSON.stringify(searchParam, null, 2));

  const pageSize = searchParam.pageSize ? (searchParam.pageSize): 5;
  const page = searchParam.page ? (searchParam.page) : 0;

  if (!pageSize || !pageSize) {
    console.log(`Trip Page: Given invalid parameters pageSize ${searchParam.pageSize} page ${searchParam.page}`);
    return <ErrorMessage message="Error loading trip page"/>
  }

  const {trips, totalPages} = await getTrips({pageSize: pageSize, pageNum: page});

  const year = 2024;

  const daysSkiedLocation = await getDaysSkied("getDaysSkiedLocation",year);

  return (
    <section className="w-full">
      <Title title="Trips"/>

      <DaysSkiedChart plotData={daysSkiedLocation} year={year}/>

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
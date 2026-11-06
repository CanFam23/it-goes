import Image from "next/image";
import Link from "next/link";
import { notFound } from "next/navigation";
import { CalendarDays, MapPin } from "lucide-react";
import Map from "@/components/Map";
import ErrorMessage from "@/components/ErrorMessage";
import TripBody from "@/components/TripBody";
import { getTrip, tripRouteCollection } from "@/lib/getTrip.mjs";
import { PublicNotFoundError } from "@/lib/publicApi.mjs";

export default async function TripDetail({ params }) {
  const { id } = await params;
  if (!/^[1-9]\d*$/.test(id)) notFound();

  let trip;
  try {
    trip = await getTrip(id);
  } catch (error) {
    if (error instanceof PublicNotFoundError) notFound();
    return <ErrorMessage message="Unable to load this trip. Please try again shortly." />;
  }

  const route = tripRouteCollection(trip);
  const tripDate = new Date(`${trip.dateOfTrip}T00:00:00`).toLocaleDateString("en-US", {
    month: "long", day: "numeric", year: "numeric",
  });
  const companions = trip.companionNames ?? [];

  return (
    <article className="w-full max-w-5xl mx-auto bg-background p-5 md:p-10 shadow-lg">
      <header className="border-b-2 border-black pb-8">
        <Link href="/trips" className="font-anon text-sm font-bold hover:underline">← All trips</Link>
        <p className="mt-8 font-anon text-sm font-bold uppercase tracking-widest text-amber-700">Trip report</p>
        <h1 className="mt-2 font-anon text-4xl sm:text-5xl md:text-6xl font-black leading-tight [overflow-wrap:anywhere]">
          {trip.title}
        </h1>

        <div className="mt-6 flex flex-wrap gap-3">
          <p className="flex items-center gap-2 rounded-full border border-black/30 bg-white px-4 py-2 font-medium">
            <MapPin size={18} aria-hidden="true" />
            {trip.locationName}, {trip.stateCountry}
          </p>
          <time dateTime={trip.dateOfTrip} className="flex items-center gap-2 rounded-full border border-black/30 bg-white px-4 py-2 font-medium">
            <CalendarDays size={18} aria-hidden="true" />
            {tripDate}
          </time>
        </div>

        <div className="mt-7 border-l-4 border-amber-500 bg-white p-5">
          <div className="flex flex-col gap-5 sm:flex-row sm:gap-10">
            <div>
              <p className="text-sm text-gray-600">Written by</p>
              <p className="mt-1 text-lg font-bold">{trip.authorName}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">With</p>
              <p className="mt-1 text-lg font-bold">
                {companions.length > 0 ? companions.join(", ") : "Solo trip"}
              </p>
            </div>
          </div>
        </div>
      </header>

      {trip.imageURL && (
        <Image src={trip.imageURL} alt={`${trip.title} cover image`}
          width={1200} height={600} className="w-full h-auto my-7 rounded-lg" />
      )}
      {!trip.imageURL && <div className="my-7 flex h-48 items-center justify-center rounded-lg bg-amber-100 text-center font-bold text-amber-900">Trip photos coming later</div>}

      {route ? (
        <section aria-label="Trip route" className="my-7">
          <h2 className="font-bold text-2xl mb-4">Route</h2>
          <Map routeData={route} fitRoute />
        </section>
      ) : (
        <p className="my-7">No route available for this trip.</p>
      )}

      <TripBody body={trip.body} description={trip.desc} />
    </article>
  );
}

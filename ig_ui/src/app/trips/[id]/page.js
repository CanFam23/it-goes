import Image from "next/image";
import Link from "next/link";
import { notFound } from "next/navigation";
import Map from "@/components/Map";
import ErrorMessage from "@/components/ErrorMessage";
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

  return (
    <article className="w-full max-w-5xl mx-auto bg-background p-5 md:p-10 shadow-lg">
      <Link href="/trips" className="hover:underline">← All trips</Link>
      <h1 className="font-black text-3xl md:text-5xl mt-5">{trip.title}</h1>
      <p className="mt-3 text-lg">{trip.locationName}, {trip.stateCountry} · {tripDate}</p>
      <p className="mt-2">By {trip.authorUsername}</p>
      {trip.peopleInvolved?.length > 0 && (
        <p className="mt-1">With {trip.peopleInvolved.join(", ")}</p>
      )}

      {trip.imageURL && (
        <Image src={trip.imageURL} alt={`${trip.title} cover image`}
          width={1200} height={600} className="w-full h-auto my-7 rounded-lg" />
      )}

      {route ? (
        <section aria-label="Trip route" className="my-7">
          <h2 className="font-bold text-2xl mb-4">Route</h2>
          <Map routeData={route} fitRoute />
        </section>
      ) : (
        <p className="my-7">No route available for this trip.</p>
      )}

      <p className="whitespace-pre-wrap text-lg leading-relaxed my-7">{trip.desc}</p>
    </article>
  );
}

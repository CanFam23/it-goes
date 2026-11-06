import Image from "next/image";
import Link from "next/link";
import { CalendarDays, MapPin } from 'lucide-react';

export default function TripSummaryCard({trip}) {
  // Convert date to more readable format for users
  // TODO error handling
  const dateOfTrip = new Date(trip.dateOfTrip).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  });

  return (
    <article className="my-5 grid w-full overflow-hidden rounded-lg bg-background shadow-lg md:my-15 md:grid-cols-[minmax(0,3fr)_minmax(0,2fr)]">
      <div className="relative aspect-video w-full overflow-hidden bg-amber-100">
        {trip.imageURL ? <Image
          src={trip.imageURL}
          alt={`${trip.title} cover image`}
          fill
          sizes="(min-width: 768px) 60vw, 100vw"
          className="object-cover"
        /> : <div className="flex h-full items-center justify-center p-4 text-center font-bold text-amber-900">Trip photos coming later</div>}
      </div>
      <div className="flex min-w-0 flex-col gap-4 p-5 sm:p-6">
        <h3 className="break-words text-xl font-black sm:text-2xl">{trip.title}</h3>
        <div className="flex flex-col gap-x-4 gap-y-2 text-sm text-stone-600 sm:text-base">
          <span className="flex min-w-0 items-start gap-2">
            <MapPin className="mt-0.5 size-5 shrink-0" aria-hidden="true" />
            <span className="break-words">{trip.locationName}, {trip.stateCountry}</span>
          </span>
          <span className="flex items-center gap-2">
            <CalendarDays className="size-5 shrink-0" aria-hidden="true" />
            <time dateTime={trip.dateOfTrip} className="whitespace-nowrap">{dateOfTrip}</time>
          </span>
        </div>
        <Link
          href={`/trips/${trip.id}`}
          className="mt-auto self-start rounded-lg border-2 border-border-btn bg-bg-btn p-2 font-bold text-text-btn transition-transform duration-200 hover:scale-105 hover:bg-gray-100"
        >Read Full Post</Link>
      </div>
    </article>
  );
}

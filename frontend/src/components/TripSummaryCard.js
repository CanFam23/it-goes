import Image from "next/image";
import Link from "next/link";
import { MapPin } from 'lucide-react';

export default function TripSummaryCard({trip}) {
  // Convert date to more readable format for users
  // TODO error handling
  const dateOfTrip = new Date(trip.dateOfTrip).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  });

  return (
    <article className="bg-background flex flex-row w-full my-5 md:my-15">
      <div className="w-full m-5 mr-0 min-w-[40vw]">
        <Image
          src={trip.imageURL}
          alt={`${trip.title} cover image`}
          width={1200}
          height={600}
          className="w-full h-auto rounded-lg -ml-8"
        />
      </div>
      <div className="py-5 -ml-6 flex flex-col items-center">
        <h3
          className="text-sm sm:text-lg md:text-2xl font-black text-center"
        >{trip.title}</h3>
        <div className="flex flex-row w-full">
          <MapPin className="w-[0.6rem] sm:w-[3rem] align-top -my-2 sm:my-0"/>
          <p className="text-[0.4rem] sm:text-md md:text-lg">{trip.locationName}, {trip.stateCountry}</p>
          <p className="text-[0.5rem] sm:text-md md:text-lg
            bg-white border-black border-1 rounded-lg px-1 text-nowrap h-fit ml-auto mr-1">{dateOfTrip}</p>
        </div>
        <p
        className="text-[0.5rem] sm:text-sm md:text-lg line-clamp-4 md:line-clamp-6"
        >{trip.desc}</p>
        <Link
          href=""
          className="text-text-btn bg-bg-btn
          text-[0.5rem] sm:text-sm md:text-lg
          font-bold border-border-btn
          border-2 rounded-lg p-1 mt-1
          transform transition-transform duration-200
          hover:scale-105 hover:bg-gray-100"
        >
          Read Full Post
        </Link>
      </div>
    </article>
  );
}